# Design: Remove fix_messages Dependency from parsed_fields Queries

## Overview

Eliminate the dependency on `fix_messages` table when querying messages from the web interface. Currently, the query uses a subquery to filter INBOUND messages from `fix_messages`. After this change, `parsed_fields` will be completely self-contained.

## Current State

### Database Schema

**parsed_fields table** already has `msg_type` field:
```sql
CREATE TABLE parsed_fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    msg_type VARCHAR(10),
    cl_ord_id VARCHAR(50),
    -- ... other fields
);
```

### Current Query Implementation

```java
// FixMessageService.java:134-186
SELECT p FROM ParsedFieldEntity p
WHERE p.messageId IN (SELECT f.id FROM FixMessageEntity f WHERE f.direction = :direction)
```

This uses a subquery to filter by INBOUND direction from the `fix_messages` table.

## Proposed Changes

### 1. Database Migration

Add `direction` field to `parsed_fields`:
```sql
ALTER TABLE parsed_fields ADD COLUMN direction ENUM('INBOUND', 'OUTBOUND')
COMMENT '报文方向：入站(INBOUND)或出站(OUTBOUND)' AFTER message_id;
```

Update existing records:
```sql
UPDATE parsed_fields SET direction = 'INBOUND';
```

Optional index for performance:
```sql
CREATE INDEX idx_parsed_field_direction ON parsed_fields(direction);
```

### 2. Entity Class Updates

**ParsedFieldEntity.java** - Add direction field:
```java
@Column(name = "direction", nullable = false)
private String direction;

public String getDirection() { return direction; }
public void setDirection(String direction) { this.direction = direction; }
```

### 3. Service Logic Changes

**FixMessageService.java** - Update `parseAndSaveFields()`:
```java
private void parseAndSaveFields(Long messageId, Message message, FixMessageEntity.MessageDirection direction) {
    ParsedFieldEntity parsedField = new ParsedFieldEntity();
    parsedField.setMessageId(messageId);
    parsedField.setDirection(direction.name());  // Add this line
    // ... rest of parsing logic
}
```

Update `saveFixMessage()` to pass direction:
```java
parseAndSaveFields(savedEntity.getId(), message, direction);
```

Simplify `queryParsedMessages()`:
```java
// Remove subquery, replace with:
SELECT p FROM ParsedFieldEntity p WHERE p.direction = 'INBOUND'
```

## Data Flow

**Current:**
```
FIX Message → saveFixMessage() → fix_messages → parseAndSaveFields() → parsed_fields
```

**New:**
```
FIX Message → saveFixMessage() → fix_messages → parseAndSaveFields() → parsed_fields (with direction)
```

## Benefits

1. **Simpler SQL** - No subqueries required
2. **Better Performance** - Single table scan instead of subquery
3. **Self-contained** - `parsed_fields` contains all query data
4. **Backward Compatible** - `fix_messages` table still exists for audit purposes

## Testing

1. **Schema Migration**: Verify column added and existing data updated
2. **Unit Tests**: Verify direction is set correctly when saving parsed fields
3. **Integration Tests**: Verify queries return correct results
4. **Performance Tests**: Compare query execution time before/after

## Implementation Checklist

- [ ] Add `direction` column to parsed_fields via SQL migration
- [ ] Update existing parsed_fields records with direction='INBOUND'
- [ ] Add `direction` field to ParsedFieldEntity class
- [ ] Update FixMessageService.parseAndSaveFields() to accept and set direction
- [ ] Simplify FixMessageService.queryParsedMessages() query
- [ ] Test with sample data
- [ ] Verify web interface queries work correctly
