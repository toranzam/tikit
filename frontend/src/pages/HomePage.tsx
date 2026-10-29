import {useAuthStore} from '../stores/authStore';
import {useState} from 'react';
import {isAxiosError} from 'axios';
import {logout} from '../api/authApi';

export default function HomePage() {
    const hasSession = useAuthStore(state => Boolean(state.tokens?.accessToken));
    const [isLoggingOut, setIsLoggingOut] = useState(false);
    const [message, setMessage] = useState('');

    async function handleLogout() {
        if (isLoggingOut) return;
        setIsLoggingOut(true);
        setMessage('');

        try {
            await logout();
        } catch (error) {
            if (isAxiosError<{message: string}>(error)) {
                setMessage(error.response?.data?.message ?? '서버에 연결할 수 없습니다.');
            } else {
                setMessage('로그아웃 처리 중 오류가 발생했습니다.');
            }
        } finally {
            setIsLoggingOut(false);
        }
    }

    return (
        <div className="mx-auto max-w-[1320px] px-5 py-10">
            <p role="status" className="text-lg">
                {hasSession ? '로그인되어 있습니다.' : '로그인이 필요합니다.'}
            </p>
            {hasSession && (
                <button
                    type="button"
                    onClick={handleLogout}
                    disabled={isLoggingOut}
                    className="mt-4 rounded-full border border-[#111] px-5 py-2 disabled:opacity-50"
                >
                    {isLoggingOut ? '로그아웃 중...' : '로그아웃'}
                </button>
            )}
            {message && <p role="alert" className="mt-2 text-sm text-red-600">{message}</p>}
        </div>
    );
}
