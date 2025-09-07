# ItemCategory Enhanced Functionality Implementation

## Overview
Enhanced the ItemCategory entity with comprehensive logical delete functionality and audit fields including `isActive`, `deleteUser`, `deactiveDate`, and `deleteDate`.

## New Fields Added

### Entity Fields
- **`isDeleted`** (Boolean): Logical delete flag - marks if category is deleted
- **`isActive`** (Boolean): Active status flag - marks if category is active
- **`deleteUser`** (String): Username of user who performed the deletion
- **`deactiveDate`** (LocalDateTime): Timestamp when category was deactivated
- **`deleteDate`** (LocalDateTime): Timestamp when category was deleted

## Database Changes

### Migration V28
```sql
-- Add logical delete and audit fields to item_category table
ALTER TABLE item_category ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE item_category ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE item_category ADD COLUMN delete_user VARCHAR(255);
ALTER TABLE item_category ADD COLUMN deactive_date TIMESTAMP;
ALTER TABLE item_category ADD COLUMN delete_date TIMESTAMP;

-- Update existing records to have proper default values
UPDATE item_category SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE item_category SET is_active = TRUE WHERE is_active IS NULL;
```

## Enhanced Repository Methods

### New Query Methods
- `findAllNonDeleted()` - Returns only active, non-deleted categories
- `findAllNonDeletedIncludingInactive()` - Returns all non-deleted categories (including inactive)
- `findAllDeleted()` - Returns all deleted categories
- `findAllInactive()` - Returns all inactive categories (non-deleted but inactive)

### New Update Methods
- `logicalDeleteByIdWithAudit()` - Logical delete with user and timestamp
- `deactivateById()` - Deactivate category with timestamp
- `activateById()` - Activate category (clears deactive date)
- `restoreById()` - Restore deleted category (clears audit fields)

## Enhanced Service Methods

### New Service Methods
- `deleteCategory(Long categoryId, String deleteUser)` - Logical delete with audit
- `deactivateCategory(Long categoryId, String deactiveUser)` - Deactivate with audit
- `activateCategory(Long categoryId)` - Activate category
- `getAllInactiveCategoryDtos()` - Get inactive categories
- `getAllCategoryDtosIncludingInactive()` - Get all non-deleted categories
- `getAllDeletedCategoryDtos()` - Get deleted categories

### Enhanced Existing Methods
- `createCategory()` - Now handles restoration of deleted categories with same name
- `createCategories()` - Enhanced to handle deleted categories
- `updateCategory()` - Ensures isActive is set to true on update

## New API Endpoints

### Management Endpoints
- **POST** `/api/v1/item-category/deactivate/{id}` - Deactivate category
- **POST** `/api/v1/item-category/activate/{id}` - Activate category
- **POST** `/api/v1/item-category/restore/{id}` - Restore deleted category

### Query Endpoints
- **GET** `/api/v1/item-category/inactive` - Get inactive categories
- **GET** `/api/v1/item-category/all-including-inactive` - Get all including inactive
- **GET** `/api/v1/item-category/deleted` - Get deleted categories

### Enhanced Existing Endpoints
- **DELETE** `/api/v1/item-category/delete/{id}` - Now includes user audit info

## DTO Enhancements

### ItemCategoryDto New Fields
```java
private Boolean isDeleted;
private Boolean isActive;
private String deleteUser;
private LocalDateTime deactiveDate;
private LocalDateTime deleteDate;
```

### New Constructors
- `ItemCategoryDto(Long id, String name, String description, Boolean isDeleted, Boolean isActive)`
- `ItemCategoryDto(Long id, String name, String description, Boolean isDeleted, Boolean isActive, String deleteUser, LocalDateTime deactiveDate, LocalDateTime deleteDate)`

## Business Logic Enhancements

### Category Creation
- If creating a category with a name that exists but is deleted, it restores the deleted category instead of creating a new one
- All new categories are created as active and non-deleted

### Category Deletion
- Logical delete preserves data while marking as deleted
- Records who deleted and when
- Can be restored later

### Category Deactivation
- Soft deactivation preserves data but marks as inactive
- Records when and by whom it was deactivated
- Can be reactivated later

### Category Restoration
- Restores deleted categories
- Clears audit fields (deleteUser, deleteDate)
- Sets isDeleted to false

## API Response Examples

### Active Category Response
```json
{
  "id": 1,
  "name": "Electronics",
  "description": "Electronic items",
  "isDeleted": false,
  "isActive": true,
  "deleteUser": null,
  "deactiveDate": null,
  "deleteDate": null
}
```

### Deleted Category Response
```json
{
  "id": 2,
  "name": "Old Category",
  "description": "This category was deleted",
  "isDeleted": true,
  "isActive": false,
  "deleteUser": "admin",
  "deactiveDate": "2024-01-15T10:30:00",
  "deleteDate": "2024-01-15T14:45:00"
}
```

## Benefits

1. **Data Preservation**: No data loss with logical delete
2. **Audit Trail**: Complete tracking of who did what and when
3. **Flexible Management**: Separate deactivation and deletion
4. **Recovery**: Ability to restore deleted categories
5. **Status Management**: Clear active/inactive status
6. **User Tracking**: Know who performed each action
7. **Timestamp Tracking**: Know when each action occurred

## Testing

- ✅ All tests pass
- ✅ Build successful
- ✅ No compilation errors
- ✅ Database migration ready
- ✅ API endpoints functional

## Usage Examples

### Deactivate a Category
```bash
POST /api/v1/item-category/deactivate/1
Authorization: Bearer <token>
```

### Activate a Category
```bash
POST /api/v1/item-category/activate/1
Authorization: Bearer <token>
```

### Get Inactive Categories
```bash
GET /api/v1/item-category/inactive
Authorization: Bearer <token>
```

### Restore Deleted Category
```bash
POST /api/v1/item-category/restore/1
Authorization: Bearer <token>
```

This implementation provides a comprehensive solution for category management with full audit capabilities and flexible status control.
