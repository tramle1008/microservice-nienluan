package com.ecommerce.payment.service;

import com.ecommerce.payment.client.OrderServiceClient;
import com.ecommerce.payment.dto.*;
import com.ecommerce.payment.exceptions.BadRequestException;
import com.ecommerce.payment.models.PaymentSession;
import com.ecommerce.payment.models.PaymentStatus;
import com.ecommerce.payment.models.Transaction;
import com.ecommerce.payment.repository.PaymentSessionRepository;
import com.ecommerce.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {



    private final PaymentSessionRepository sessionRepo;
    private final TransactionRepository txRepo;
    private final TransactionRepository transactionRepository;
    private final OrderServiceClient orderClient;

    private final String bankAccount = "108876614804";     // THAY BẰNG SỐ TK SEPAY CỦA BẠN- tôi sẽ làm sau
    private final String bankName = "VietinBank";


    @Override
    public QRPaymentResponseDTO initPayment(Long userId, Long orderId) {
        // 1. LẤY ORDER ĐỂ CHECK STATUS + LẤY SỐ TIỀN
        OrderDTO order = orderClient.getOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("Không tìm thấy đơn hàng: " + orderId);
        }

        // CHUYỂN String → enum (nếu cần)
        OrderStatus status = order.getStatus();
        if (status == null) {
            try {
                status = OrderStatus.valueOf(order.getStatus().toString().trim());
            } catch (Exception e) {
                throw new BadRequestException("Trạng thái đơn hàng không hợp lệ");
            }
        }

        if (!Set.of(OrderStatus.PENDING_PAYMENT, OrderStatus.CONFIRMED).contains(status)) {
            throw new BadRequestException("Đơn hàng không hợp lệ để tạo thanh toán QR");
        }
        // 3. TẠO SESSION
        PaymentSession session = new PaymentSession();
        session.setOrderId(orderId);
        session.setUserId(userId);
        session.setAmount(order.getFinalAmount());
        session.setCurrency("VND");
        session.setStatus("PENDING");
        session.setPgName("SePay");
        session.setTransactionCode(generateTxCode());
        session.setExpiresAt(Timestamp.from(Instant.now().plus(10, ChronoUnit.MINUTES)));
        session.setCreatedAt(Timestamp.from(Instant.now()));
        session.setUpdatedAt(Timestamp.from(Instant.now()));
        sessionRepo.save(session);

        // 4. TẠO QR URL
        String qrUrl = generateQrUrl(order.getFinalAmount(), session.getTransactionCode());

        log.info("Tạo QR thành công cho order {} - user {} - txCode {}", orderId, userId, session.getTransactionCode());

        return new QRPaymentResponseDTO(session.getTransactionCode(), order.getFinalAmount(), qrUrl);
    }

    @Override
    public void handleSePayWebhook(SePayWebhookDTO dto) {
        log.info("Nhận webhook từ SePay: {}", dto);

        if (!"in".equalsIgnoreCase(dto.transferType())) {
            log.info("Bỏ qua giao dịch out");
            return;
        }

        String txCode = extractTxCode(dto.content());
        if (txCode == null) {
            log.warn("Không tìm thấy transaction code trong nội dung: {}", dto.content());
            return;
        }

        PaymentSession session = sessionRepo.findByTransactionCode(txCode).orElse(null);
        if (session == null) {
            log.warn("Không tìm thấy session cho txCode: {}", txCode);
            return;
        }

        if ("SUCCESS".equals(session.getStatus())) {
            log.info("Session {} đã xử lý trước đó", txCode);
            return;
        }

        if (session.getExpiresAt().before(Timestamp.from(Instant.now()))) {
            session.setStatus("EXPIRED");
            sessionRepo.save(session);
            log.info("Session {} đã hết hạn", txCode);
            return;
        }

        BigDecimal amount = new BigDecimal(dto.transferAmount().replace(",", ""));
        if (session.getAmount().compareTo(amount) != 0) {
            log.warn("Số tiền không khớp: expect {} - got {}", session.getAmount(), amount);
            return;
        }
        saveTransaction(dto);
        // GỌI ORDER SERVICE ĐÁNH DẤU ĐÃ THANH TOÁN

        try {
            orderClient.markPaid(session.getOrderId());
            session.setStatus("SUCCESS");
            session.setUpdatedAt(Timestamp.from(Instant.now()));
            sessionRepo.save(session);
            System.out.println(("Thanh toán thành công - order: {} - txCode: {}"));

        } catch (Exception e) {
            log.error("Lỗi khi gọi markPaid cho order {}: {}", session.getOrderId(), e.getMessage());

        }
    }

    @Override
    public PaymentStatus getPaymentStatus(String transactionCode) {
        PaymentSession session = sessionRepo.findByTransactionCode(transactionCode).orElse(null);
        if ("SUCCESS".equals(session.getStatus())) return PaymentStatus.PAID;
        else return PaymentStatus.UNPAID;
    }

    // === HELPER METHODS ===
    private String generateQrUrl(BigDecimal amount, String txCode) {
        String desc = "SEVQR" + txCode;
        String encoded = URLEncoder.encode(desc, StandardCharsets.UTF_8);
        return String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%d&des=%s",
                bankAccount, bankName, amount.longValueExact(), encoded);
    }

    private String generateTxCode() {
        return "TX" + System.nanoTime() % 1000000 + RandomStringUtils.randomAlphanumeric(4).toUpperCase();
    }

    private String extractTxCode(String content) {
        if (content == null || content.isBlank()) return null;
        Matcher m = Pattern.compile("SEVQR([A-Z0-9]+)").matcher(content);
        return m.find() ? m.group(1) : null;
    }

    private void saveTransaction(SePayWebhookDTO dto) {
        Transaction tx = new Transaction();
        tx.setGateway("SePay");
        tx.setTransactionDate(Timestamp.valueOf(dto.transactionDate()));
        tx.setAccountNumber(dto.accountNumber());
        tx.setSubAccount(dto.subAccount());
        tx.setTransactionContent(dto.content());
        tx.setReferenceNumber(dto.referenceCode());
        tx.setBody(dto.description());
        tx.setCode(dto.code());

        BigDecimal amount = new BigDecimal(dto.transferAmount().replace(",", ""));
        tx.setAmountIn("in".equalsIgnoreCase(dto.transferType()) ? amount : BigDecimal.ZERO);
        tx.setAmountOut("out".equalsIgnoreCase(dto.transferType()) ? amount : BigDecimal.ZERO);
        tx.setAccumulated(new BigDecimal(dto.accumulated().replace(",", "")));

        txRepo.save(tx);
    }

    private String extractOrderCode(String content) {
        if (content == null || content.isBlank()) return null;
        Pattern pattern = Pattern.compile("DH ? ?(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? "DH" + matcher.group(1) : null;
    }
}