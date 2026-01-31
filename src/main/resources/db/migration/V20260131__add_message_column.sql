-- Migration: Add message column to parsed_messages table
-- Date: 2026-01-31

ALTER TABLE parsed_messages ADD COLUMN message VARCHAR(2000) COMMENT '原始FIX报文' AFTER ord_type;
