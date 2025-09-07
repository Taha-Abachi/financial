# Database Migration: Company-GiftCard Relationship

## Overview
This migration adds a company relationship to gift cards, allowing gift cards to be associated with specific companies and enabling company-based validation and reporting.

## Migration Files

### V14__add_company_to_giftcard.sql
- Adds `company_id` column to `gift_card` table
- Creates foreign key constraint to `company` table
- Adds index for performance optimization

## Migration Process

1. **Run the migrations in order:**
   ```bash
   # The migrations will run automatically when the application starts
   # or you can run them manually using Flyway
   ```

2. **Verify the migration:**
   ```sql
   -- Check if the column was added
   DESCRIBE gift_card;
   
   -- Check if the foreign key constraint exists
   SELECT * FROM information_schema.table_constraints 
   WHERE table_name = 'gift_card' AND constraint_type = 'FOREIGN KEY';
   
   -- Check if the index was created
   SHOW INDEX FROM gift_card;
   ```

## Impact

### Database Changes
- New column: `gift_card.company_id` (BIGINT, nullable)
- New foreign key constraint: `fk_giftcard_company`
- New index: `idx_giftcard_company`

### Application Changes
- GiftCard entity now has a `@ManyToOne` relationship with Company
- New validation logic ensures gift cards can only be used in stores belonging to the same company
- New API endpoints for company-related operations

## Testing

After migration, test the following:
1. Create a new company
2. Assign a gift card to the company
3. Verify that the gift card can only be used in stores belonging to the same company
4. Test company-based reporting endpoints

## Notes

- The `company_id` column is nullable, allowing existing gift cards to remain unassigned
- The migration includes sample companies for testing
- The rollback migration safely removes all changes if needed 