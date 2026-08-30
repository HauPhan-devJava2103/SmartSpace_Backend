-- --------------------------------------------------------
-- Tệp dữ liệu mẫu (Seed Data) cho SmartSpace_Backend
-- Đảm bảo bạn đã chạy Hibernate để tạo cấu trúc bảng trước khi import file này, hoặc các table đã tồn tại
-- --------------------------------------------------------

-- Lưu ý: Mật khẩu của các user được mã hoá Bcrypt là 'Ad123456!'
-- Tài khoản đăng nhập mẫu:
-- 1/ admin@gmail.com | Ad123456!
-- 2/ user@gmail.com | Ad123456!
-- 3/ cudan@gmail.com | Ad123456!

SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------------------
-- Xóa toàn bộ dữ liệu cũ theo thứ tự ngược của Foreign Keys
-- --------------------------------------------------------
DELETE FROM message_media;
DELETE FROM chat_messages;
DELETE FROM conversation_participants;
DELETE FROM conversations;
DELETE FROM device_tokens;
DELETE FROM user_notification_states;
DELETE FROM notifications;
DELETE FROM reports;
DELETE FROM users;

-- --------------------------------------------------------
-- Table: users
-- Phụ thuộc: (Không)
-- --------------------------------------------------------
INSERT INTO users (id, created_at, updated_at, full_name, date_of_birth, phone, email, password, gender, role, status, avatar_url) VALUES
('u1', NOW(), NOW(), 'Nguyễn Văn Admin', '1990-01-01', '0901234567', 'admin@gmail.com', '$2a$10$nosRTfjEU6rYa5Ps58DdEuEnns.WJrUKgAWR56/lpphULiTLUrJqy', 'male', 'admin', 'active', 'https://ui-avatars.com/api/?name=AD&background=6366f1&color=fff&size=200&bold=true&font-size=0.4'),
('u2', NOW(), NOW(), 'Trần Thị User', '1995-05-15', '0912345678', 'user@gmail.com', '$2a$10$nosRTfjEU6rYa5Ps58DdEuEnns.WJrUKgAWR56/lpphULiTLUrJqy', 'female', 'client', 'active', 'https://ui-avatars.com/api/?name=US&background=6366f1&color=fff&size=200&bold=true&font-size=0.4'),
('u3', NOW(), NOW(), 'Lê Hữu Cư Dân', '1992-10-20', '0923456789', 'cudan@gmail.com', '$2a$10$nosRTfjEU6rYa5Ps58DdEuEnns.WJrUKgAWR56/lpphULiTLUrJqy', 'male', 'client', 'active', 'https://ui-avatars.com/api/?name=CD&background=6366f1&color=fff&size=200&bold=true&font-size=0.4');

-- --------------------------------------------------------
-- Table: reports
-- Phụ thuộc: users
-- --------------------------------------------------------
INSERT INTO reports (id, created_at, updated_at, title, description, image_url, latitude, longitude, severity, status, user_id) VALUES
('r1', NOW(), NOW(), 'Đèn đường hỏng', 'Cột đèn số 12 bị hỏng không sáng vào ban đêm', 'https://picsum.photos/400/300?random=1', 10.762622, 106.660172, 'LOW', 'PENDING', 'u2'),
('r2', NOW(), NOW(), 'Rác thải đổ bừa bãi', 'Khu vực công viên có người đổ rác bừa bãi gây bốc mùi', 'https://picsum.photos/400/300?random=2', 10.763622, 106.661172, 'MEDIUM', 'PROCESSING', 'u3'),
('r3', NOW(), NOW(), 'Tai nạn giao thông', 'Tai nạn ở ngã tư, cần hỗ trợ khẩn cấp', 'https://picsum.photos/400/300?random=3', 10.764622, 106.662172, 'CRITICAL', 'PENDING', 'u2'),
('r4', NOW(), NOW(), 'Đường ống nước vỡ', 'Nước ngập tràn ra đường hẻm 45', 'https://picsum.photos/400/300?random=4', 10.765622, 106.663172, 'HIGH', 'PROCESSED', 'u3');

-- --------------------------------------------------------
-- Table: notifications
-- Phụ thuộc: users
-- --------------------------------------------------------
INSERT INTO notifications (id, created_at, updated_at, title, message, is_read, user_id) VALUES
('n1', NOW(), NOW(), 'Bảo trì hệ thống', 'Hệ thống sẽ bảo trì vào 00:00 ngày mai', 0, NULL),
('n2', NOW(), NOW(), 'Phản ánh đang được xử lý', 'Phản ánh "Rác thải đổ bừa bãi" của bạn đang được xử lý', 0, 'u3'),
('n3', NOW(), NOW(), 'Cảnh báo kẹt xe', 'Kẹt xe nghiêm trọng tại ngã tư XYZ', 0, 'u2');

-- --------------------------------------------------------
-- Table: user_notification_states
-- Phụ thuộc: users, notifications
-- --------------------------------------------------------
INSERT INTO user_notification_states (id, created_at, updated_at, is_read, notification_id, user_id) VALUES
('uns1', NOW(), NOW(), 0, 'n1', 'u1'),
('uns2', NOW(), NOW(), 1, 'n1', 'u2'),
('uns3', NOW(), NOW(), 0, 'n1', 'u3');

-- --------------------------------------------------------
-- Table: device_tokens
-- Phụ thuộc: users
-- --------------------------------------------------------
INSERT INTO device_tokens (id, created_at, updated_at, fcm_token, platform, device_name, last_used_at, user_id) VALUES
('dt1', NOW(), NOW(), 'sample-fcm-token-user-2-web', 'web', 'Web Browser', NOW(), 'u2'),
('dt2', NOW(), NOW(), 'sample-fcm-token-user-3-android', 'android', 'Android Phone', NOW(), 'u3');

-- --------------------------------------------------------
-- Table: conversations
-- Phụ thuộc: (Không)
-- --------------------------------------------------------
INSERT INTO conversations (id, name, conversation_avatar, conversation_type, participant_hash, created_at, last_message_id, last_message_content, last_message_time) VALUES
('c1', 'Trần Thị User, Lê Hữu Cư Dân', NULL, 'PRIVATE', 'u2_u3', NOW(), 'm2', 'Chào bạn, mình thấy tin phản ánh của bạn.', NOW());

-- --------------------------------------------------------
-- Table: conversation_participants
-- Phụ thuộc: conversations, users
-- --------------------------------------------------------
INSERT INTO conversation_participants (id, conversation_id, user_id, joined_at) VALUES
('cp1', 'c1', 'u2', NOW()),
('cp2', 'c1', 'u3', NOW());

-- --------------------------------------------------------
-- Table: chat_messages
-- Phụ thuộc: conversations, users
-- --------------------------------------------------------
INSERT INTO chat_messages (id, conversation_id, sender_id, content, message_type, sent_at) VALUES
('m1', 'c1', 'u2', 'Xin chào!', 'text', DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
('m2', 'c1', 'u3', 'Chào bạn, mình thấy tin phản ánh của bạn.', 'text', DATE_SUB(NOW(), INTERVAL 2 MINUTE));

-- --------------------------------------------------------
-- Table: message_media
-- Phụ thuộc: chat_messages
-- --------------------------------------------------------
INSERT INTO message_media (id, message_id, file_name, thumbnail_url, file_type, uploaded_at) VALUES
(1, 'm1', 'sample_image.jpg', 'https://picsum.photos/400/300?random=5', 'IMAGE', NOW());

SET FOREIGN_KEY_CHECKS = 1;
