# README Update Prompt - Scan for Recent Changes

## Purpose
This prompt guides the process of scanning the Spring Boot E-Commerce project for recent changes and updating the README.md file with new information about what has been implemented.

## Instructions

### 1. **Scan Project for Changes**

Run the following command to see recent changes/modifications:
```bash
cd "/Users/mymac/Documents/coding stuff/Spring-Apps/spring-boot-full-stack/sb-ecomm"

# Check for recently modified Java files
find src/main/java -type f -name "*.java" -mtime -7 -exec ls -lh {} \; 2>/dev/null

# Alternative: Check git status if available
git status

# Check git log for recent commits
git log --oneline --all -20

# Check specific directories for recent changes
echo "=== Recent Changes in src/main/java ==="
find src/main/java -type f -name "*.java" -exec stat -f "%Sm %N" {} \; 2>/dev/null | sort -r

# Check if pom.xml was recently modified (new dependencies)
stat -f "%Sm %N" pom.xml 2>/dev/null

# List all Java files in the project
echo "=== All Java Files in Project ==="
find src/main/java -type f -name "*.java" | sort
```

### 2. **Analyze Changes**

For each recent change, answer these questions:

- **What was added/modified?** (controller, service, model, config, etc.)
- **Why was it changed?** (feature implementation, refactoring, bug fix, etc.)
- **What files were affected?** (list all modified files)
- **When was it last modified?** (check file timestamps)
- **Is it a breaking change?** (does it affect existing API or architecture?)
- **Does it align with vertical slice architecture?** (is it organized correctly?)

### 3. **Examine Key Locations for Recent Changes**

Check these areas for modifications:

```
✅ Controllers (src/main/java/com/echapps/ecom/project/*/controller/)
   - New endpoints added?
   - Request/response changes?
   - Validation changes?

✅ Services (src/main/java/com/echapps/ecom/project/*/service/)
   - Business logic changes?
   - New methods?
   - Refactoring?

✅ Models/Entities (src/main/java/com/echapps/ecom/project/*/model/)
   - New fields?
   - New annotations?
   - Validation changes?

✅ DTOs (src/main/java/com/echapps/ecom/project/*/dto/)
   - New DTOs?
   - DTO structure changes?
   - Request/Response updates?

✅ Repositories (src/main/java/com/echapps/ecom/project/*/repository/)
   - New query methods?
   - Custom query implementations?

✅ Configuration (src/main/java/com/echapps/ecom/project/config/)
   - New constants?
   - Configuration changes?

✅ Exceptions (src/main/java/com/echapps/ecom/project/exceptions/)
   - New exception handlers?
   - Exception changes?

✅ Shared Components (src/main/java/com/echapps/ecom/project/ - root level)
   - New utilities?
   - Framework changes?

✅ pom.xml
   - New dependencies?
   - Version updates?

✅ application.properties
   - Configuration changes?
   - New properties?
```

### 4. **Document Findings**

Create a summary of changes in this format:

```markdown
## Latest Updates (February XX, 2026):

- 🔹 **Feature/Component Name**
  - Change 1: Description
  - Change 2: Description
  - Benefits: Why this matters
  - Files affected: List of modified files

- 🔹 **Another Feature**
  - Change 1: Description
  - Rationale: Why was this done?
```

### 5. **Update README.md Structure**

When updating the README, follow this section order in the "🔄 Recent Changes" area:

1. **Latest Updates (Current Date)** - At the top
2. **Previous Updates** - Older changes pushed down
3. **Key Features** section - Update ✅ checklist
4. **Project Structure** - Add new files/directories if needed
5. **API Endpoints** - Add new endpoints
6. **Architecture Overview** - Update if structure changed

### 6. **Specific Areas to Check**

#### For Category Feature
- CategoryController.java
- CategoryService.java / CategoryServiceImpl.java
- Category.java model
- CategoryRequest.java / CategoryResponse.java DTOs
- CategoryRepository.java

#### For Product Feature
- ProductController.java
- ProductService.java / ProductServiceImpl.java
- Product.java model
- ProductRequest.java / ProductResponse.java DTOs
- ProductRepository.java

#### For Global Changes
- AppConstants.java
- GlobalExceptionHandler.java
- APIException.java
- ResourceNotFoundException.java
- pom.xml

### 7. **Formatting Guidelines for README**

Use these emojis for consistency:
- 🔹 General changes
- ✅ Completed features
- 🚧 In development
- 📄 DTO/Response changes
- 📊 Pagination/Sorting features
- ⚙️ Configuration changes
- 🔍 Repository/Query changes
- 🔄 Refactoring
- 🛡️ Validation changes
- 🏷️ Feature/Component names
- 💾 Database changes
- 🏗️ Architecture changes

### 8. **What NOT to Update**

- Don't remove old "Previous Updates" sections (keep history)
- Don't change sections not affected by the changes
- Don't speculate about changes - only document what was actually modified
- Don't remove completed feature items from "Key Features" section

### 9. **Final Validation**

Before finalizing:
- [ ] All changes are documented
- [ ] Changes follow chronological order (newest first)
- [ ] Each change includes "Why" and "What" explanation
- [ ] Benefits are clearly stated
- [ ] File list is accurate
- [ ] Emojis are used consistently
- [ ] Links/references are correct
- [ ] Project structure diagram updated if needed
- [ ] API Endpoints section updated if applicable
- [ ] "Key Features" checklist is current

### 10. **Quick Command Reference**

```bash
# View all Java files with modification times (newest first)
ls -lhtr src/main/java/com/echapps/ecom/project/**/*.java 2>/dev/null | tail -20

# Find files modified in last 24 hours
find src/main/java -type f -name "*.java" -mtime -1

# Show what changed (if git available)
git diff HEAD~5..HEAD --name-only

# Count lines of code
find src/main/java -name "*.java" | xargs wc -l | tail -1

# Search for recent TODOs or FIXMEs
grep -r "TODO\|FIXME" src/main/java --include="*.java" | head -20
```

## Usage

To use this prompt:

1. **Copy this entire prompt** and use it with your AI assistant
2. **Paste it with instructions** like: "Using this prompt, scan the project for recent changes and update the README"
3. **Provide context** about when the last README update was (e.g., "Last updated on February 17, 2026")
4. **Get results** with a detailed summary of changes ready for the README

## Example Output Format

```markdown
## Latest Updates (February 23, 2026):

- 📄 **New DTO Implementation for Product**
  - Created ProductRequest DTO for POST/PUT requests
  - Created ProductResponse DTO for GET responses
  - Added validation annotations to ProductRequest
  - Updated ProductController to use new DTOs
  - Files affected: ProductRequest.java, ProductResponse.java, ProductController.java
  - Benefits: Consistent API contract pattern matching Category feature

- ⚙️ **Enhanced Configuration Constants**
  - Added product-related constants to AppConstants
  - Defined pagination defaults for product endpoints
  - Files affected: AppConstants.java
  - Benefits: Centralized configuration for product feature
```

## Notes

- This README is a **living document** - update it every time you make significant changes
- Keep the history of changes so readers can see project evolution
- Be specific and detailed in change descriptions
- Include "why" not just "what" for each change
- Update the "Key Features" section checklist appropriately
