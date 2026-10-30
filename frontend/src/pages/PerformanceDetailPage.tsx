import {Link, useParams} from 'react-router'
import {mockPerformances} from '../mocks/performances'

export default function PerformanceDetailPage() {
  const {performanceId} = useParams()

  const performance = mockPerformances.find(
    item => item.id === performanceId,
  )

  if (!performance) {
    return (
      <main className="mx-auto max-w-[1320px] px-5 py-20 text-center">
        <h1 className="text-2xl font-bold">
          공연 정보를 찾을 수 없습니다.
        </h1>
        <Link to="/" className="mt-6 inline-block underline">
          메인으로 이동
        </Link>
      </main>
    )
  }

  return (
    <main className="mx-auto max-w-[1320px] px-5 py-12">
      <Link
        to={`/categories/${performance.categoryId}`}
        className="text-sm text-gray-500 hover:underline"
      >
        ← 공연 목록으로
      </Link>

      <div className="mt-8 grid gap-10 md:grid-cols-[320px_1fr]">
        <div
          aria-hidden="true"
          className="aspect-[3/4] rounded-xl bg-[#999]"
        />

        <section>
          <h1 className="text-3xl font-bold">
            {performance.title}
          </h1>

          <dl className="mt-8 grid grid-cols-[80px_1fr] gap-y-5 text-base">
            <dt className="font-semibold">공연 장소</dt>
            <dd>{performance.venue}</dd>

            <dt className="font-semibold">공연 기간</dt>
            <dd>{performance.period}</dd>
          </dl>
        </section>
      </div>
    </main>
  )
}