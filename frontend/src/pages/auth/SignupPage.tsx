export default function SignupPage() {
    return (
        <section
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
                        autoComplete="email"
                        placeholder="이메일"
                        className="h-[60px] w-full rounded-[15px] border border-[#999] bg-white px-5 py-[10px] text-sm leading-[normal] placeholder:text-[#999]"
                    />
                </label>

                <label className="block">
                    <span className="sr-only">이름</span>
                    <input
                        type="text"
                        name="name"
                        autoComplete="name"
                        placeholder="이름"
                        className="h-[60px] w-full rounded-[15px] border border-[#999] bg-white px-5 py-[10px] text-sm leading-[normal] placeholder:text-[#999]"
                    />
                </label>

                <label className="block">
                    <span className="sr-only">비밀번호</span>
                    <input
                        type="password"
                        name="password"
                        autoComplete="new-password"
                        placeholder="비밀번호"
                        className="h-[60px] w-full rounded-[15px] border border-[#999] bg-white px-5 py-[10px] text-sm leading-[normal] placeholder:text-[#999]"
                    />
                </label>

                <label className="block">
                    <span className="sr-only">비밀번호 확인</span>
                    <input
                        type="password"
                        name="passwordConfirm"
                        autoComplete="new-password"
                        placeholder="비밀번호 확인"
                        className="h-[60px] w-full rounded-[15px] border border-[#999] bg-white px-5 py-[10px] text-sm leading-[normal] placeholder:text-[#999]"
                    />
                </label>

                <label className="flex cursor-pointer items-start gap-1 p-1 text-xs leading-[normal] text-black">
                    <input
                        type="checkbox"
                        name="termsAgreed"
                        className="relative h-[15px] w-[15px] shrink-0 cursor-pointer appearance-none rounded-[5px] border border-[#999] checked:border-[#111] checked:bg-[#111] checked:after:absolute checked:after:top-px checked:after:left-1 checked:after:h-2 checked:after:w-[5px] checked:after:rotate-45 checked:after:border-r-[1.5px] checked:after:border-b-[1.5px] checked:after:border-white checked:after:content-['']"
                    />
                    <span>이용약관 및 개인정보처리방침에 동의합니다.</span>
                </label>
            </div>

            <button
                type="button"
                disabled
                className="h-[45px] w-full rounded-[25px] bg-[#999] px-5 text-base leading-[normal] text-white"
            >
                가입
            </button>
        </section>
    );
}
