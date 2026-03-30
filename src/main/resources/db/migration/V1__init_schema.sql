CREATE TABLE coupons (
    id UUID PRIMARY KEY,
    code_normalized VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    max_redemptions INTEGER NOT NULL CHECK (max_redemptions > 0),
    current_redemptions INTEGER NOT NULL CHECK (current_redemptions >= 0),
    country_code VARCHAR(2) NOT NULL,
    CONSTRAINT uk_coupons_code_normalized UNIQUE (code_normalized),
    CONSTRAINT chk_coupons_country_code_format CHECK (country_code ~ '^[A-Z]{2}$'),
    CONSTRAINT chk_coupons_redemptions_bound CHECK (current_redemptions <= max_redemptions)
);

CREATE TABLE coupon_redemptions (
    id UUID PRIMARY KEY,
    coupon_id UUID NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    redeemed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_coupon_redemptions_coupon_id
        FOREIGN KEY (coupon_id) REFERENCES coupons (id) ON DELETE CASCADE,
    CONSTRAINT uk_coupon_redemptions_coupon_user UNIQUE (coupon_id, user_id)
);

CREATE INDEX idx_coupon_redemptions_coupon_id ON coupon_redemptions (coupon_id);
