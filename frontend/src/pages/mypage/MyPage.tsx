import { Link } from 'react-router'
import { useEffect, useState } from 'react'
import { getMyProfile, type MemberProfile } from '../../api/memberApi'

// 사이드바 메뉴
const bookingMenus = [
    '예매 내역',
    '취소/환불 내역',
    '관심 공연',
]

const accountMenus = [
    '내 정보 조회',
    '비밀번호 변경',
    '알림 설정',
    '로그아웃',
]

// <<임시>> 공연 데이터
const bookings = [
    { id: 1, title: '뮤지컬 공연 예시', date: '2026.11.15', status: '예매 완료' },
    { id: 2, title: '연극 공연 예시', date: '2026.11.20', status: '예매 완료' },
]

const favorites = [
    { id: 1, title: '관심 공연 1' },
    { id: 2, title: '관심 공연 2' },
    { id: 3, title: '관심 공연 3' },
]

// 마이페이지 메인 화면
export default function MyPage() {
    const [profile, setProfile] = useState<MemberProfile | null>(null)

    // 회원정보 조회
    useEffect(() => {
        const loadProfile = async () => {
            try {
                const profileData = await getMyProfile()
                setProfile(profileData)
            } catch (error) {
                console.error('회원정보 조회 실패: ', error)
            }
        }

        void loadProfile()
    }, [])

    return (
        <div className="mx-auto w-full max-w-[1320px] px-5 py-10">
            <h1 className="border-b border-[#333] pb-5 text-[28px] font-semibold">
                마이페이지
            </h1>

            <div className="mt-8 flex flex-col gap-10 md:flex-row">
                <aside className="w-full shrink-0 md:w-[190px]">
                    <div>
                        <h2 className="mb-5 text-[15px] font-semibold">
                            나의 예매 정보
                        </h2>

                        <nav className="flex flex-col gap-4">
                            {bookingMenus.map((menu) => (
                                // TODO: 예매 내역(김민성님), 취소/환불 내역(최정환님), 관심 공연(김기현님) 이동할 수 있게 해야 함
                                <span
                                    key={menu}
                                    className="text-[14px] text-[#555]"
                                >
                                    {menu}
                                </span>
                            ))}
                        </nav>
                    </div>

                    <div className="my-8 border-t border-[#ddd]" />

                    <div>
                        <h2 className="mb-5 text-[15px] font-semibold">
                            계정 관리
                        </h2>

                        <nav className="flex flex-col gap-4">
                            {accountMenus.map((menu) =>
                                    menu === '내 정보 조회' ? (
                                        <Link
                                            key={menu}
                                            to="/mypage/profile"
                                            className="text-[14px] text-[#555] hover:text-black"
                                        >
                                            {menu}
                                        </Link>
                                    ) : (
                                        // TODO: 비번 변경, 알림 설정, 로그아웃 등 이동/작동할 수 있게 해야 함
                                        <span
                                            key={menu}
                                            className="text-[14px] text-[#555]"
                                        >
                                            {menu}
                                        </span>
                                    )
                            )}
                        </nav>
                    </div>
                </aside>

                <div className="min-w-0 flex-1">
                    <section className="flex flex-col justify-between gap-6 rounded-md border border-[#ddd] bg-[#fafafa] p-7 sm:flex-row sm:items-center">
                        <div>
                            <h2 className="text-[22px] font-semibold">
                                {profile ? `${profile.name}님` : '회원정보 불러오는 중...'}
                            </h2>
                            <p className="mt-2 text-[14px] text-[#777]">
                                {profile?.email ?? ''}
                            </p>
                        </div>

                        <div className="flex gap-10">
                            <div className="text-center">
                                <p className="text-[13px] text-[#666]">관람 예정</p>
                                <p className="mt-2 text-[20px] font-semibold">
                                    2 <span className="text-[13px] font-normal">건</span>
                                </p>
                            </div>

                            <div className="text-center">
                                <p className="text-[13px] text-[#666]">관심 공연</p>
                                <p className="mt-2 text-[20px] font-semibold">
                                    3 <span className="text-[13px] font-normal">건</span>
                                </p>
                            </div>
                        </div>
                    </section>

                    {/* 예매 내역 */}
                    <section className="mt-12">
                        <div className="mb-4 flex items-center justify-between">
                            <h2 className="text-[20px] font-semibold">예매 내역</h2>
                            <span className="text-[13px] text-[#777]">
                                전체 보기 ›
                              </span>
                        </div>

                        <div className="bg-[#f5f5f5] px-5 py-3 text-[13px] text-[#666]">
                            최근 3개월 예매 내역
                        </div>

                        {bookings.map((booking) => (
                            <div
                                key={booking.id}
                                className="flex items-center justify-between gap-4 border-b border-[#eee] px-5 py-5"
                            >
                                <div>
                                    <p className="text-[15px] font-medium">
                                        {booking.title}
                                    </p>
                                    <p className="mt-2 text-[13px] text-[#888]">
                                        {booking.date}
                                    </p>
                                </div>

                                <span className="text-[13px] text-[#555]">
                                  {booking.status}
                                </span>
                            </div>
                        ))}
                    </section>

                    {/* 관심 공연 */}
                    <section className="mt-12">
                        <div className="mb-4 flex items-center justify-between">
                            <h2 className="text-[20px] font-semibold">관심 공연</h2>
                            <span className="text-[13px] text-[#777]">
                                전체 보기 ›
                              </span>
                        </div>

                        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                            {favorites.map((favorite) => (
                                <div key={favorite.id}>
                                    {/* TODO: 추후 실제 공연 포스터로 교체 */}
                                    <div className="flex aspect-[3/4] items-center justify-center rounded-md bg-[#f1f1f1] text-[13px] text-[#999]">
                                        공연 이미지
                                    </div>

                                    <p className="mt-3 text-[14px] font-medium">
                                        {favorite.title}
                                    </p>
                                </div>
                            ))}
                        </div>
                    </section>

                    {/* 계정 관리 */}
                    <section className="mt-12">
                        <h2 className="mb-4 text-[20px] font-semibold">
                            계정 관리
                        </h2>

                        <div className="grid grid-cols-1 border border-[#ddd] sm:grid-cols-3">
                            <Link
                                to="/mypage/profile"
                                className="flex items-center justify-between border-b border-[#ddd] px-5 py-6 text-[14px] sm:border-r sm:border-b-0"
                            >
                                내 정보 조회
                                <span>›</span>
                            </Link>

                            <div className="flex items-center justify-between border-b border-[#ddd] px-5 py-6 text-[14px] sm:border-r sm:border-b-0">
                                비밀번호 변경
                                <span>›</span>
                            </div>

                            <div className="flex items-center justify-between px-5 py-6 text-[14px]">
                                알림 설정
                                <span>›</span>
                            </div>
                        </div>
                    </section>
                </div>
            </div>
        </div>
    )
}
