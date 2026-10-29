import {useNavigate} from "react-router";
import {useState} from "react";
import type {ChangeEvent, SubmitEvent} from "react";
import {signup} from "../../api/authApi.ts";
import {isAxiosError} from "axios";


const INIT_SIGNUP_FORM = {
    email: '',
    name: '',
    password: '',
    passwordConfirm: '',
    termsAgreed: false,
};

export default function SignupPage() {

    const navigate = useNavigate();

    const [signupForm, setSignupForm] = useState(INIT_SIGNUP_FORM);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [message, setMessage] = useState('');

    function handleChange(e: ChangeEvent<HTMLInputElement>) {
        const {name, value, type, checked} = e.currentTarget;

        setSignupForm(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value,
        }));
    }

    function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
        e.preventDefault();

        if (isSubmitting) return;

        setMessage('');

        if (
            !signupForm.email.trim() ||
            !signupForm.name.trim() ||
            !signupForm.password ||
            !signupForm.passwordConfirm
        ) {
            setMessage('회원가입 정보를 모두 입력해주세요.');
            return;
        }

        if (/\s/.test(signupForm.password)) {
            setMessage('비밀번호에는 공백을 사용할 수 없습니다.');
            return;
        }

        if(signupForm.password !== signupForm.passwordConfirm) {
            setMessage('비밀번호가 일치하지 않습니다.');
            return;
        }

        if(!signupForm.termsAgreed) {
            setMessage('이용약관 및 개인정보처리방침에 동의해주세요.');
            return;
        }

        setIsSubmitting(true);

        signup({
            email: signupForm.email.trim(),
            name: signupForm.name.trim(),
            password: signupForm.password
        })
            .then(() => {
                setSignupForm(INIT_SIGNUP_FORM);
                navigate('/login', { replace: true });
            })
            .catch((err: unknown)=> {
                if(isAxiosError<{message: string}>(err)) {
                    setMessage(
                        err.response?.data?.message ?? '회원가입 요청을 처리할 수 없습니다.'
                    );
                } else {
                    setMessage('회원가입 처리 중 오류가 발생했습니다.');
                }
            })
            .finally(() => {
                setIsSubmitting(false);
            })

    }



    return (
        <form
            onSubmit={handleSubmit}
            aria-labelledby="signup-title"
            className="mx-auto flex w-full max-w-[620px] flex-col gap-6 px-10 pt-40 pb-16 max-sm:px-6 max-sm:pt-16"
        >
            <div className="flex flex-col gap-[10px] py-1">
                <h1
                    id="signup-title"
                    className="text-2xl leading-[normal] font-semibold"
                >
                    TIKIT 회원가입
                </h1>
                <p className="text-[15px] leading-[normal]">
                    회원가입을 위한 정보를 입력해주세요.
                </p>
            </div>

            <div className="flex flex-col gap-5">
                <label className="block">
                    <span className="sr-only">이메일</span>
                    <input
                        type="email"
                        name="email"
                        value={signupForm.email}
                        onChange={handleChange}
                        disabled={isSubmitting}
                        required
                        autoComplete="email"
                        placeholder="이메일"
                        className="h-[60px] w-full rounded-[15px] border border-[#999] bg-white px-5 py-[10px] text-sm leading-[normal] placeholder:text-[#999] focus:outline-none!"
                    />
                </label>

                <label className="block">
                    <span className="sr-only">이름</span>
                    <input
                        type="text"
                        name="name"
                        value={signupForm.name}
                        onChange={handleChange}
                        disabled={isSubmitting}
                        required
                        autoComplete="name"
                        placeholder="이름"
                        className="h-[60px] w-full rounded-[15px] border border-[#999] bg-white px-5 py-[10px] text-sm leading-[normal] placeholder:text-[#999] focus:outline-none!"
                    />
                </label>

                <label className="block">
                    <span className="sr-only">비밀번호</span>
                    <input
                        type="password"
                        name="password"
                        value={signupForm.password}
                        onChange={handleChange}
                        disabled={isSubmitting}
                        required
                        autoComplete="new-password"
                        placeholder="비밀번호"
                        className="h-[60px] w-full rounded-[15px] border border-[#999] bg-white px-5 py-[10px] text-sm leading-[normal] placeholder:text-[#999] focus:outline-none!"
                    />
                </label>

                <label className="block">
                    <span className="sr-only">비밀번호 확인</span>
                    <input
                        type="password"
                        name="passwordConfirm"
                        value={signupForm.passwordConfirm}
                        onChange={handleChange}
                        disabled={isSubmitting}
                        required
                        autoComplete="new-password"
                        placeholder="비밀번호 확인"
                        className="h-[60px] w-full rounded-[15px] border border-[#999] bg-white px-5 py-[10px] text-sm leading-[normal] placeholder:text-[#999] focus:outline-none!"
                    />
                </label>

                <label className="flex cursor-pointer items-start gap-1 p-1 text-xs leading-[normal] text-black">
                    <input
                        type="checkbox"
                        name="termsAgreed"
                        checked={signupForm.termsAgreed}
                        onChange={handleChange}
                        disabled={isSubmitting}
                        className="relative h-[15px] w-[15px] shrink-0 cursor-pointer appearance-none rounded-[5px] border border-[#999] checked:border-[#111] checked:bg-[#111] checked:after:absolute checked:after:top-px checked:after:left-1 checked:after:h-2 checked:after:w-[5px] checked:after:rotate-45 checked:after:border-r-[1.5px] checked:after:border-b-[1.5px] checked:after:border-white checked:after:content-['']"
                    />
                    <span>이용약관 및 개인정보처리방침에 동의합니다.</span>
                </label>
            </div>

            <div className="flex flex-col gap-[10px]">
                {message && (
                    <p role="alert" className="text-sm text-red-600">
                        {message}
                    </p>
                )}
                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="h-[45px] w-full rounded-[25px] bg-[#999] px-5 text-base leading-[normal] text-white"
                >
                    {isSubmitting ? '가입 중...' : '가입'}
                </button>
            </div>
        </form>
    );
}
