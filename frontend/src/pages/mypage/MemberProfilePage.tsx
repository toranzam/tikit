import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import {
    getMyProfile,
    updateMyProfile,
    type MemberProfile,
} from '../../api/memberApi'

// 가입일 표시 형식 : YYYY-MM-DD
function formatDate(value: string) {
    return new Date(value).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
    }).replace(/\. /g, '-').replace('.', '')
}

export default function MemberProfilePage() {
    const navigate = useNavigate()

    const [profile, setProfile] = useState<MemberProfile | null>(null)
    const [name, setName] = useState('')
    const [isEditing, setIsEditing] = useState(false)
    const [isLoading, setIsLoading] = useState(true)
    const [isSaving, setIsSaving] = useState(false)
    const [message, setMessage] = useState('')
    const [error, setError] = useState('')

    useEffect(() => {
        let active = true

        async function loadProfile() {
            try {
                const data = await getMyProfile()

                if (!active) return

                setProfile(data)
                setName(data.name)
            } catch {
                if (active) setError('회원정보를 불러오지 못했습니다.')
            } finally {
                if (active) setIsLoading(false)
            }
        }

        void loadProfile()

        return () => {
            active = false
        }
    }, [])

    // 이름 변경 시작
    function handleEdit() {
        setIsEditing(true)
        setMessage('')
        setError('')
    }

    // 변경사항 저장
    async function handleSave() {
        if (!profile) return

        if (!isEditing) {
            navigate('/mypage')
            return
        }

        const trimmedName = name.trim()

        if (!trimmedName) {
            setError('이름을 입력해 주세요.')
            return
        }

        if (trimmedName.length > 50) {
            setError('이름은 50자 이하로 입력해 주세요.')
            return
        }

        setIsSaving(true)
        setError('')
        setMessage('')

        try {
            const updated = await updateMyProfile(trimmedName)
            setProfile(updated)
            setName(updated.name)
            setIsEditing(false)
            setMessage('회원정보가 수정되었습니다.')
        } catch {
            setError('회원정보 수정에 실패했습니다. 다시 시도해 주세요.')
        } finally {
            setIsSaving(false)
        }
    }

    // 수정 취소 또는 마이페이지 복귀
    function handleCancel() {
        if (isEditing && profile) {
            setName(profile.name)
            setIsEditing(false)
            setError('')
            setMessage('')
        } else {
            navigate('/mypage')
        }
    }

    return (
        <div className="mx-auto w-full max-w-[1320px] px-5 py-10">
            {/* 페이지 제목 */}
            <h1 className="border-b border-[#333] pb-5 text-[28px] font-semibold">
                마이페이지
            </h1>

            <div className="mt-8 flex flex-col gap-10 md:flex-row">
                {/* 왼쪽 사이드바 */}
                <aside className="w-full shrink-0 md:w-[190px]">
                    <h2 className="mb-5 text-[15px] font-semibold">나의 예매 정보</h2>
                    <nav className="flex flex-col gap-4 text-[14px] text-[#555]">
                        <span>예매 내역</span>
                        <span>취소/환불 내역</span>
                        <span>관심 공연</span>
                    </nav>

                    <div className="my-8 border-t border-[#ddd]" />

                    <h2 className="mb-5 text-[15px] font-semibold">계정 관리</h2>
                    <nav className="flex flex-col gap-4 text-[14px] text-[#555]">
                        <Link to="/mypage/profile"
                              className="rounded bg-[#eeeeee] px-2 py-2 text-[14px] text-[#111]">
                            회원 정보 조회
                        </Link>
                        <span>비밀번호 변경</span>
                        <span>알림 설정</span>
                        <span>로그아웃</span>
                    </nav>
                </aside>

                <section className="min-w-0 flex-1">
                    <h2 className="mb-6 text-[22px] font-semibold">회원 정보 조회</h2>

                    {isLoading ? (
                        <p className="text-[14px] text-[#777]">회원정보를 불러오는 중...</p>
                    ) : profile ? (
                        <>
                            <div className="rounded-md bg-[#f7f7f7] px-8 py-7">
                                <div className="flex items-center border-b border-[#ddd] py-5">
                                    <label htmlFor="member-name" className="w-28 shrink-0 text-[15px] font-medium">
                                        이름
                                    </label>

                                    {isEditing ? (
                                        <input
                                            id="member-name"
                                            value={name}
                                            maxLength={50}
                                            onChange={(event) => setName(event.target.value)}
                                            className="min-w-0 flex-1 rounded border border-[#bbb] bg-white px-3 py-2 text-[15px]"
                                        />
                                    ) : (
                                        <span id="member-name" className="flex-1 text-[15px]">
                                            {profile.name}
                                        </span>
                                    )}

                                    {!isEditing && (
                                        <button
                                            type="button"
                                            onClick={handleEdit}
                                            className="ml-4 text-[14px] font-medium underline"
                                        >
                                            변경
                                        </button>
                                    )}
                                </div>

                                <div className="flex items-center border-b border-[#ddd] py-5">
                                    <span className="w-28 shrink-0 text-[15px] font-medium">
                                        이메일
                                    </span>
                                    <span className="break-all text-[15px]">{profile.email}</span>
                                </div>


                                <div className="flex items-center py-5">
                                    <span className="w-28 shrink-0 text-[15px] font-medium">
                                        가입일
                                    </span>
                                    <span className="text-[15px]">
                                        {formatDate(profile.createdAt)}
                                    </span>
                                </div>
                            </div>

                            {/* 처리 결과 메시지 */}
                            {message && (
                                <p role="status" className="mt-4 text-[14px] text-green-700">
                                    {message}
                                </p>
                            )}
                            {error && (
                                <p role="alert" className="mt-4 text-[14px] text-red-600">
                                    {error}
                                </p>
                            )}

                            <div className="mt-7 flex justify-end gap-3">
                                <button
                                    type="button"
                                    onClick={handleCancel}
                                    disabled={isSaving}
                                    className="rounded border border-[#ccc] px-8 py-3 text-[14px] disabled:opacity-50"
                                >
                                    취소
                                </button>
                                <button
                                    type="button"
                                    onClick={() => void handleSave()}
                                    disabled={isSaving}
                                    className="rounded bg-[#333] px-8 py-3 text-[14px] text-white disabled:opacity-50"
                                >
                                    {isSaving ? '저장 중...' : '저장'}
                                </button>
                            </div>
                        </>
                    ) : (
                        <div>
                            <p role="alert" className="text-[14px] text-red-600">
                                {error || '회원정보를 표시할 수 없습니다.'}
                            </p>
                            <Link to="/mypage" className="mt-4 inline-block text-[14px] underline">
                                마이페이지로 돌아가기
                            </Link>
                        </div>
                    )}
                </section>
            </div>
        </div>
    )
}
