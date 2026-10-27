export default function LoginPage() {
  return (
    <section aria-labelledby="login-title" className="mx-auto flex w-full max-w-[620px] flex-col gap-6 px-10 pt-40 pb-16 max-sm:px-6 max-sm:pt-16">
      <div className="flex flex-col gap-[10px] py-1">
        <h1 id="login-title" className="text-2xl leading-[normal] font-semibold">TIKIT 로그인</h1>
        <p className="text-[15px] leading-[normal]">가입하실 때 사용한 이메일과 비밀번호를 입력해주세요.</p>
      </div>
      <div className="flex flex-col gap-5">
        <label className="block">
          <span className="sr-only">이메일</span>
          <input type="email" name="email" autoComplete="email" placeholder="이메일" className="h-[60px] w-full rounded-[15px] border border-[#999] bg-white px-5 py-[10px] text-sm leading-[normal] placeholder:text-[#999]" />
        </label>
        <label className="block">
          <span className="sr-only">비밀번호</span>
          <input type="password" name="password" autoComplete="current-password" placeholder="비밀번호" className="h-[60px] w-full rounded-[15px] border border-[#999] bg-white px-5 py-[10px] text-sm leading-[normal] placeholder:text-[#999]" />
        </label>
      </div>
      <div className="flex flex-col gap-[10px]">
        <button type="button" disabled className="h-[45px] w-full rounded-[25px] bg-[#999] px-5 text-base leading-[normal] text-white">다음</button>
        <button type="button" className="h-[45px] w-full rounded-[25px] border border-[#111] px-5 text-base leading-[normal]">회원 가입</button>
        <button type="button" className="self-end px-5 text-sm leading-[normal] text-[#716f6f]">비밀번호를 잊으셨나요?</button>
      </div>
    </section>
  )
}
