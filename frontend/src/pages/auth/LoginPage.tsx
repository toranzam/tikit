import {useState} from "react";
import type {ChangeEvent, SubmitEvent} from "react";
import {login} from "../../api/authApi.ts";
import * as axios from "axios";
import {Link, useNavigate} from "react-router";
import {useAuthStore} from "../../stores/authStore.ts";

const INIT_LOGIN_FORM = {
    email: '',
    password: ''
}

export default function LoginPage() {

    const navigate = useNavigate();
    const setTokens = useAuthStore(state => state.setTokens);

    const [loginForm, setLoginForm] = useState(INIT_LOGIN_FORM);
    // 제출 중인지
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [message, setMessage] = useState('');

    function handleChange(e: ChangeEvent<HTMLInputElement>) {
        const {name, value} = e.currentTarget;

        setLoginForm(prev => ({
            ...prev,
            [name]: value,
        }))
    }

    function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
        e.preventDefault();

        if (isSubmitting) return;

        setIsSubmitting(true);
        setMessage('');

        login(loginForm)
            .then(res => {
                setTokens(res.data)
                setLoginForm(INIT_LOGIN_FORM);
                navigate("/", {replace: true});
            })
            .catch((err: unknown) => {
                if (axios.isAxiosError<{ message: string }>(err)) {
                    setMessage(
                        err.response?.data?.message ?? '서버에 연결할 수 없습니다.'
                    )

                } else {
                    setMessage('로그인 처리 중 오류가 발생했습니다.')
                }
            })
            .finally(() => {
                setIsSubmitting(false);
            })
    }


    return (
        <form
            onSubmit={handleSubmit}
            aria-labelledby="login-title"
            className="mx-auto flex w-full max-w-[620px] flex-col gap-6 px-10 pt-40 pb-16 max-sm:px-6 max-sm:pt-16">
            <div className="flex flex-col gap-[10px] py-1">
                <h1
                    id="login-title"
                    className="text-2xl leading-[normal] font-semibold">
                    TIKIT 로그인
                </h1>
                <p
                    className="text-[15px] leading-[normal]">
                    가입하실 때 사용한 이메일과 비밀번호를 입력해주세요.
                </p>
            </div>
            <div className="flex flex-col gap-5">
                <label className="block">
                    <span className="sr-only">이메일</span>
                    <input type="email"
                           name="email"
                           autoComplete="email"
                           placeholder="이메일"
                           disabled={isSubmitting}
                           onChange={handleChange}
                           value={loginForm.email}
                           required
                           className="h-[60px] w-full rounded-[15px] border border-[#999] bg-white px-5 py-[10px] text-sm leading-[normal] placeholder:text-[#999]"
                    />
                </label>
                <label className="block">
                    <span className="sr-only">비밀번호</span>
                    <input type="password"
                           name="password"
                           autoComplete="current-password"
                           placeholder="비밀번호"
                           disabled={isSubmitting}
                           onChange={handleChange}
                           value={loginForm.password}
                           required
                           className="h-[60px] w-full rounded-[15px] border border-[#999] bg-white px-5 py-[10px] text-sm leading-[normal] placeholder:text-[#999]"/>
                </label>
            </div>
            <div className="flex flex-col gap-[10px]">
                {message && (
                    <p role={"status"} className={"text-sm text-red-600"}>
                        {message}
                    </p>
                )}
                <button
                    type="submit"
                    disabled={
                        isSubmitting || !loginForm.email.trim() || !loginForm.password
                    }
                    className="h-[45px] w-full rounded-[25px] bg-[#999] px-5 text-base leading-[normal] text-white">
                    {isSubmitting ? '로그인 중...' : '로그인'}
                </button>
                <Link
                    to="/signup"
                    className="flex items-center justify-center h-[45px] w-full rounded-[25px] border border-[#111] px-5 text-base leading-[normal]">
                    회원 가입
                </Link>
                <button
                    type="button"
                    className="self-end px-5 text-sm leading-[normal] text-[#716f6f]">
                    비밀번호를 잊으셨나요?
                </button>
            </div>
        </form>
    )
}
