import type {Performance} from '../../mocks/performances'

interface PerformanceCardProps {
  performance: Performance
  variant?: 'opening' | 'standard'
}

export default function PerformanceCard({performance, variant = 'standard'}: PerformanceCardProps) {
  return (
    <article className="min-w-0 text-[#111]">
      <div aria-hidden="true" className="aspect-[235.2/313.59] w-full rounded-xl bg-[#999]" />
      <div className="flex flex-col gap-3 pt-4 pr-2 pl-1 leading-[normal]">
        {variant === 'opening' && <p className="text-lg leading-[normal] font-semibold">{performance.openingLabel}</p>}
        <h3 className="text-lg leading-[normal] font-semibold break-words">{performance.title}</h3>
        {variant === 'standard' && (
          <div className="flex flex-col gap-1 text-sm leading-[normal]">
            <p>{performance.venue}</p>
            <p>{performance.period}</p>
          </div>
        )}
      </div>
    </article>
  )
}
