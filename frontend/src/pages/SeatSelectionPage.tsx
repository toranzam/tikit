import { useEffect, useState } from "react";
import { useParams } from "react-router";
import { getSessionSeats } from "../api/sessionSeatApi";
import type { SessionSeat } from "../api/sessionSeatApi";

// 공연 회차별 좌석 선택 페이지
export default function SeatSelectionPage() {

    // URL에서 공연 회차 ID 가져오기
    const { sessionId } = useParams();

    // 좌석 목록 상태
    const [seats, setSeats] = useState<SessionSeat[]>([]);

    // 로딩 상태
    const [loading, setLoading] = useState(true);

    // 오류 메시지 상태
    const [error, setError] = useState<string | null>(null);

    // 공연 회차 ID가 변경되면 좌석 목록 조회
    useEffect(() => {
        let cancelled = false;

        async function loadSeats() {
            setLoading(true);
            setError(null);
            setSeats([]);

            // 공연 회차 ID 유효성 검사
            const id = Number(sessionId);

            if (!sessionId || !Number.isSafeInteger(id) || id <= 0) {
                setError("올바르지 않은 공연 회차 ID입니다.");
                setLoading(false);
                return;
            }

            try {
                // 백엔드 좌석 조회 API 호출
                const data = await getSessionSeats(id);

                if (!cancelled) {
                    setSeats(data);
                }
            } catch {
                if (!cancelled) {
                    setError("좌석 정보를 불러오지 못했습니다.");
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        void loadSeats();

        // 이전 요청 결과가 뒤늦게 반영되는 것을 방지
        return () => {
            cancelled = true;
        };
    }, [sessionId]);

    return (
        <div>
            {/* 페이지 제목 */}
            <h1>공연 좌석 선택</h1>

            {/* 공연 회차 ID */}
            <p>공연 회차 ID: {sessionId}</p>

            {/* 로딩 메시지 */}
            {loading && <p>좌석 정보를 불러오는 중입니다...</p>}

            {/* 오류 메시지 */}
            {!loading && error && <p role="alert">{error}</p>}

            {/* 좌석이 없는 경우 */}
            {!loading && !error && seats.length === 0 && (
                <p>등록된 좌석이 없습니다.</p>
            )}

            {/* 좌석 목록 표시 */}
            {!loading && !error && seats.length > 0 && (
                <div>
                    <h2>좌석 목록</h2>

                    {seats.map((seat) => (
                        <div key={seat.seatId}>
                            <p>
                                {seat.label} /
                                등급: {seat.grade} /
                                가격: {seat.price.toLocaleString()}원 /
                                상태: {seat.status}
                            </p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}