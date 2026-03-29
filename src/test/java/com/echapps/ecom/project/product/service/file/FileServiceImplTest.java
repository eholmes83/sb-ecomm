package com.echapps.ecom.project.product.service.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FileServiceImpl layer.
 *
 * Testing Framework Choice: JUnit 5 (Jupiter) + Mockito + @TempDir
 * Rationale:
 * - JUnit 5 @TempDir provides an isolated, auto-cleaned temporary filesystem for each test,
 *   ensuring tests never pollute the real project directory
 * - Mockito mocks MultipartFile to control filename and InputStream without real HTTP uploads
 * - FileServiceImpl has no injected dependencies, so it is instantiated directly in @BeforeEach
 * - Nested test classes group tests by behaviour area, matching the project's existing test style
 *
 * Test Coverage:
 * - Happy paths: file created, content correct, extension preserved, UUID prefix applied
 * - Directory handling: directory created when absent, no error when directory already exists
 * - Filename construction: single extension, multiple dots, different extensions, uniqueness
 * - Error cases: IOException from InputStream, NPE from null filename, no-extension filename
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileServiceImpl Unit Tests")
class FileServiceImplTest {

    @TempDir
    Path tempDir;

    @Mock
    private MultipartFile multipartFile;

    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl();
    }

    // ===========================================================================
    // uploadImage – returned filename
    // ===========================================================================

    @Nested
    @DisplayName("uploadImage - returned filename")
    class ReturnedFilenameTests {

        @Test
        @DisplayName("Should return a filename that ends with the same extension as the original file (.jpg)")
        void shouldReturnFilenameWithSameExtensionJpg() throws IOException {
            // Arrange
            when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
            when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("image data".getBytes()));

            // Act
            String result = fileService.uploadImage(tempDir.toString(), multipartFile);

            // Assert
            assertNotNull(result);
            assertTrue(result.endsWith(".jpg"), "Returned filename should end with .jpg");
        }

        @Test
        @DisplayName("Should return a filename that ends with the same extension as the original file (.png)")
        void shouldReturnFilenameWithSameExtensionPng() throws IOException {
            // Arrange
            when(multipartFile.getOriginalFilename()).thenReturn("banner.png");
            when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("png data".getBytes()));

            // Act
            String result = fileService.uploadImage(tempDir.toString(), multipartFile);

            // Assert
            assertNotNull(result);
            assertTrue(result.endsWith(".png"), "Returned filename should end with .png");
        }

        @Test
        @DisplayName("Should return a filename different from the original, prefixed with a valid UUID")
        void shouldReturnFilenameWithValidUUIDPrefix() throws IOException {
            // Arrange
            when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
            when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("image data".getBytes()));

            // Act
            String result = fileService.uploadImage(tempDir.toString(), multipartFile);

            // Assert
            assertNotEquals("photo.jpg", result, "Returned filename should not equal the original name");
            String uuidPart = result.substring(0, result.lastIndexOf('.'));
            assertDoesNotThrow(() -> UUID.fromString(uuidPart),
                    "The portion before the extension should be a valid UUID");
        }

        @Test
        @DisplayName("Should use only the last extension segment when the original filename contains multiple dots")
        void shouldExtractLastExtensionFromFilenameWithMultipleDots() throws IOException {
            // Arrange
            when(multipartFile.getOriginalFilename()).thenReturn("my.product.image.png");
            when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

            // Act
            String result = fileService.uploadImage(tempDir.toString(), multipartFile);

            // Assert
            assertTrue(result.endsWith(".png"),
                    "Should use the last extension (.png) when there are multiple dots in the filename");
        }

        @Test
        @DisplayName("Should produce a unique filename on every call (no collisions)")
        void shouldProduceUniqueFilenameOnEachCall() throws IOException {
            // Arrange
            when(multipartFile.getOriginalFilename()).thenReturn("image.jpg");
            when(multipartFile.getInputStream())
                    .thenReturn(new ByteArrayInputStream("content-one".getBytes()))
                    .thenReturn(new ByteArrayInputStream("content-two".getBytes()));

            // Act
            String firstName = fileService.uploadImage(tempDir.toString(), multipartFile);
            String secondName = fileService.uploadImage(tempDir.toString(), multipartFile);

            // Assert
            assertNotEquals(firstName, secondName, "Each upload should produce a unique filename");
        }
    }

    // ===========================================================================
    // uploadImage – file creation and content
    // ===========================================================================

    @Nested
    @DisplayName("uploadImage - file creation and content")
    class FileCreationTests {

        @Test
        @DisplayName("Should create the uploaded file at the path derived from the returned filename")
        void shouldCreateFileAtReturnedPath() throws IOException {
            // Arrange
            when(multipartFile.getOriginalFilename()).thenReturn("document.pdf");
            when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("pdf content".getBytes()));

            // Act
            String fileName = fileService.uploadImage(tempDir.toString(), multipartFile);

            // Assert
            Path expectedPath = tempDir.resolve(fileName);
            assertTrue(Files.exists(expectedPath), "Uploaded file should exist at the path built from the returned filename");
        }

        @Test
        @DisplayName("Should write the exact bytes from the InputStream to the created file")
        void shouldWriteCorrectContentToFile() throws IOException {
            // Arrange
            byte[] fileContent = "test image content 12345".getBytes();
            when(multipartFile.getOriginalFilename()).thenReturn("image.jpg");
            when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));

            // Act
            String fileName = fileService.uploadImage(tempDir.toString(), multipartFile);

            // Assert
            byte[] writtenContent = Files.readAllBytes(tempDir.resolve(fileName));
            assertArrayEquals(fileContent, writtenContent, "Written file content should match the original input stream bytes");
        }

        @Test
        @DisplayName("Should handle an empty file (zero-byte content) without error")
        void shouldHandleEmptyFileContent() throws IOException {
            // Arrange
            when(multipartFile.getOriginalFilename()).thenReturn("empty.jpg");
            when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

            // Act
            String fileName = fileService.uploadImage(tempDir.toString(), multipartFile);

            // Assert
            Path uploadedPath = tempDir.resolve(fileName);
            assertTrue(Files.exists(uploadedPath), "File should be created even when content is empty");
            assertEquals(0, Files.size(uploadedPath), "Empty file should have size 0");
        }
    }

    // ===========================================================================
    // uploadImage – directory handling
    // ===========================================================================

    @Nested
    @DisplayName("uploadImage - directory handling")
    class DirectoryHandlingTests {

        @Test
        @DisplayName("Should create the upload directory when it does not already exist")
        void shouldCreateDirectoryWhenItDoesNotExist() throws IOException {
            // Arrange
            Path nonExistentDir = tempDir.resolve("new-upload-folder");
            assertFalse(Files.exists(nonExistentDir), "Pre-condition: directory must not exist before upload");

            when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
            when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

            // Act
            fileService.uploadImage(nonExistentDir.toString(), multipartFile);

            // Assert
            assertTrue(Files.exists(nonExistentDir), "Directory should be created by uploadImage");
        }

        @Test
        @DisplayName("Should upload successfully when the directory already exists")
        void shouldUploadSuccessfullyWhenDirectoryAlreadyExists() throws IOException {
            // Arrange
            Path existingDir = tempDir.resolve("existing-folder");
            Files.createDirectory(existingDir);
            assertTrue(Files.exists(existingDir), "Pre-condition: directory must exist before upload");

            when(multipartFile.getOriginalFilename()).thenReturn("image.jpg");
            when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

            // Act & Assert
            assertDoesNotThrow(() -> fileService.uploadImage(existingDir.toString(), multipartFile),
                    "uploadImage should not throw when the directory already exists");
        }

        @Test
        @DisplayName("Should place the uploaded file inside the specified directory")
        void shouldPlaceFileInsideSpecifiedDirectory() throws IOException {
            // Arrange
            Path subDir = tempDir.resolve("products");

            when(multipartFile.getOriginalFilename()).thenReturn("item.jpg");
            when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

            // Act
            String fileName = fileService.uploadImage(subDir.toString(), multipartFile);

            // Assert
            File uploadedFile = new File(subDir.toString(), fileName);
            assertTrue(uploadedFile.exists(), "The uploaded file should reside inside the specified directory");
        }
    }

    // ===========================================================================
    // uploadImage – error handling
    // ===========================================================================

    @Nested
    @DisplayName("uploadImage - error handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should propagate IOException when the InputStream throws during copy")
        void shouldPropagateIOExceptionFromInputStream() throws IOException {
            // Arrange
            when(multipartFile.getOriginalFilename()).thenReturn("image.jpg");
            when(multipartFile.getInputStream()).thenThrow(new IOException("Stream read error"));

            // Act & Assert
            IOException exception = assertThrows(IOException.class, () ->
                    fileService.uploadImage(tempDir.toString(), multipartFile));
            assertEquals("Stream read error", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when getOriginalFilename() returns null")
        void shouldThrowNullPointerExceptionWhenOriginalFilenameIsNull() {
            // Arrange
            // getOriginalFilename() returns null – calling .substring() on it causes NPE
            when(multipartFile.getOriginalFilename()).thenReturn(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    fileService.uploadImage(tempDir.toString(), multipartFile));
        }

        @Test
        @DisplayName("Should throw StringIndexOutOfBoundsException when the filename has no extension")
        void shouldThrowExceptionWhenFilenameHasNoExtension() {
            // Arrange
            // lastIndexOf('.') returns -1 → substring(-1) throws StringIndexOutOfBoundsException
            when(multipartFile.getOriginalFilename()).thenReturn("filewithoutextension");

            // Act & Assert
            assertThrows(StringIndexOutOfBoundsException.class, () ->
                    fileService.uploadImage(tempDir.toString(), multipartFile));
        }
    }
}
