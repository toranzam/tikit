import { createBrowserRouter } from "react-router";
import SeatSelectionPage from "../pages/SeatSelectionPage";

// 애플리케이션의 페이지 경로를 관리하는 라우터
const router = createBrowserRouter([
    {
        // 메인 페이지
        path: "/",
        element: <div>Hello World</div>,
    },
    {
        // 공연 회차별 좌석 선택 페이지
        // :sessionId는 URL에서 공연 회차 ID를 전달받는 동적 경로
        // 예시: /sessions/1/seats
        path: "/sessions/:sessionId/seats",
        element: <SeatSelectionPage />,
    },
]);

export default router;