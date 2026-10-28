import {useAuthStore} from '../stores/authStore';

export default function HomePage() {
    const hasSession = useAuthStore(state => Boolean(state.tokens?.accessToken));

    return (
        <div className="mx-auto max-w-[1320px] px-5 py-10">
            <p role="status" className="text-lg">
                {hasSession ? '로그인되어 있습니다.' : '로그인이 필요합니다.'}
            </p>
        </div>
    );
}
