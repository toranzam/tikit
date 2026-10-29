import {useState} from 'react'
import {popularCategories} from '../constants/categories'
import type {CategoryId} from '../constants/categories'
import {mockOpeningPerformances, mockPerformances} from '../mocks/performances'
import PerformanceBanner from '../components/performance/PerformanceBanner'
import PerformanceSection from '../components/performance/PerformanceSection'
import PromotionBanner from '../components/performance/PromotionBanner'

export default function HomePage() {
  const [selectedCategory, setSelectedCategory] = useState<CategoryId>('theater')
  const popularPerformances = mockPerformances.filter(performance => performance.categoryId === selectedCategory)

  return (
    <div className="pb-20">
      <h1 className="sr-only">TIKIT 공연</h1>
      <PerformanceBanner />
      <div className="mx-auto max-w-[1320px]">
        <PerformanceSection title="오픈 예정" performances={mockOpeningPerformances.slice(0, 10)} variant="opening" />
        <PerformanceSection key={selectedCategory} title="TIKIT 장르별 인기" performances={popularPerformances}>
          <div aria-label="인기 공연 장르" className="flex gap-2 overflow-x-auto py-1 -my-1">
            {popularCategories.map(category => (
              <button
                key={category.id}
                type="button"
                aria-pressed={selectedCategory === category.id}
                onClick={() => setSelectedCategory(category.id)}
                className={`min-w-20 shrink-0 rounded-full border border-[#e5e5ec] px-4 py-[11px] text-base leading-[normal] font-semibold ${selectedCategory === category.id ? 'bg-[#111] text-white' : 'bg-white text-[#111]'}`}
              >
                {category.label}
              </button>
            ))}
          </div>
        </PerformanceSection>
        <PromotionBanner />
      </div>
    </div>
  )
}
