package com.tikit.payment.client;

import com.tikit.payment.dto.toss.TossConfirmRequestDTO;
import com.tikit.payment.dto.toss.TossPaymentResponseDTO;
import com.tikit.payment.exception.TossApiException;
import com.tikit.payment.exception.TossResultUnknownException;
import com.tikit.payment.exception.TossUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class TossPaymentClientTest {

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String IDEMPOTENCY_KEY = "test-idempotency-key";

    private MockRestServiceServer mockServer;
    private TossPaymentClient tossPaymentClient;
    private TossConfirmRequestDTO request;

    @BeforeEach
    void setup() {
        // 가짜 Toss 서버를 RestClient에 연결 (실제 Toss로 요청이 나가지 않음)
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.tosspayments.com");
        mockServer = MockRestServiceServer.bindTo(builder).build();

        tossPaymentClient = new TossPaymentClient(builder.build(), JsonMapper.builder().build());
        request = new TossConfirmRequestDTO("test-payment-key", "test-order-1", 55000L);
    }

    @Test
    @DisplayName("승인에 성공하면 Toss 결제 정보를 반환하고, 요청에 멱등키와 승인 정보를 담아 보낸다")
    void confirm_payment_success() {
        // given
        // setup에서 이미 셋팅
        mockServer.expect(requestTo(CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Idempotency-Key", IDEMPOTENCY_KEY))
                .andExpect(jsonPath("$.paymentKey").value("test-payment-key"))
                .andExpect(jsonPath("$.orderId").value("test-order-1"))
                .andExpect(jsonPath("$.amount").value(55000))
                .andRespond(withSuccess("""
                        {
                          "paymentKey": "test-payment-key",
                          "orderId": "test-order-1",
                          "status": "DONE",
                          "method": "카드",
                          "totalAmount": 55000,
                          "balanceAmount": 55000,
                          "lastTransactionKey": "test-transaction-key",
                          "requestedAt": "2026-10-29T10:00:00+09:00",
                          "approvedAt": "2026-10-29T10:00:05+09:00"
                        }
                        """, MediaType.APPLICATION_JSON));

        // when
        TossPaymentResponseDTO response = tossPaymentClient.confirmPayment(request, IDEMPOTENCY_KEY);
        System.out.println("테스트: " + response);

        // then
        mockServer.verify();
        assertThat(response.status()).isEqualTo("DONE");
        assertThat(response.orderId()).isEqualTo("test-order-1");
        assertThat(response.totalAmount()).isEqualTo(55000L);
        assertThat(response.approvedAt()).isNotNull();
    }

    @Test
    @DisplayName("Toss가 승인을 거절하면 Toss 에러 코드를 담은 TossApiException이 발생한다")
    void confirm_payment_declined() {
        // given
        // setup에서 이미 셋팅
        mockServer.expect(requestTo(CONFIRM_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "code": "REJECT_CARD_PAYMENT",
                                  "message": "한도초과 혹은 잔액부족으로 결제에 실패했습니다."
                                }
                                """));

        // when
        Throwable thrown = catchThrowable(() -> tossPaymentClient.confirmPayment(request, IDEMPOTENCY_KEY));
        System.out.println("테스트: " + thrown);

        // then
        assertThat(thrown).isInstanceOf(TossApiException.class);
        TossApiException e = (TossApiException) thrown;
        assertThat(e.getHttpStatus()).isEqualTo(400);
        assertThat(e.getTossCode()).isEqualTo("REJECT_CARD_PAYMENT");
    }

    @Test
    @DisplayName("Toss 서버 장애(5xx)면 TossUnavailableException이 발생한다")
    void confirm_payment_toss_server_error() {
        // given
        // setup에서 이미 셋팅
        mockServer.expect(requestTo(CONFIRM_URL))
                .andRespond(withServerError());

        // when
        Throwable thrown = catchThrowable(() -> tossPaymentClient.confirmPayment(request, IDEMPOTENCY_KEY));
        System.out.println("테스트: " + thrown);

        // then
        assertThat(thrown).isInstanceOf(TossUnavailableException.class);
    }

    @Test
    @DisplayName("Toss 서버에 연결하지 못하면 TossUnavailableException이 발생한다")
    void confirm_payment_connection_failed() {
        // given
        // setup에서 이미 셋팅
        mockServer.expect(requestTo(CONFIRM_URL))
                .andRespond(withException(new ConnectException("Connection refused")));

        // when
        Throwable thrown = catchThrowable(() -> tossPaymentClient.confirmPayment(request, IDEMPOTENCY_KEY));
        System.out.println("테스트: " + thrown);

        // then
        assertThat(thrown).isInstanceOf(TossUnavailableException.class);
    }

    @Test
    @DisplayName("요청 후 응답을 받지 못하면 TossResultUnknownException이 발생한다")
    void confirm_payment_result_unknown() {
        // given
        // setup에서 이미 셋팅
        mockServer.expect(requestTo(CONFIRM_URL))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        // when
        Throwable thrown = catchThrowable(() -> tossPaymentClient.confirmPayment(request, IDEMPOTENCY_KEY));
        System.out.println("테스트: " + thrown);

        // then
        assertThat(thrown).isInstanceOf(TossResultUnknownException.class);
    }
}