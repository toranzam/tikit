-- 판매자(SELLER) 역할을 없애고 공연 관리자(MANAGER) 역할 도입.
-- 기존 SELLER 계정은 공연 관리 권한을 자동으로 받지 않도록 일반 회원으로 변경.
-- MANAGER 계정은 ADMIN이 별도로 생성.
UPDATE members
SET role = 'MEMBER'
WHERE role = 'SELLER';

ALTER TABLE members
    DROP CONSTRAINT ck_members_role;

ALTER TABLE members
    ADD CONSTRAINT ck_members_role CHECK (role IN ('MEMBER', 'MANAGER', 'ADMIN'));
