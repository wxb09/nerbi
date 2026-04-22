import type { Directive } from 'vue'

export const vInfiniteScroll: Directive<
  HTMLElement,
  () => void
> = {
  mounted(el, binding) {
    const callback = binding.value
    if (!callback || typeof callback !== 'function') return

    el.addEventListener('scroll', () => {
      const scrollTop = el.scrollTop
      const clientHeight = el.clientHeight
      const scrollHeight = el.scrollHeight

      if (scrollTop + clientHeight >= scrollHeight - 100) {
        callback()
      }
    }, { passive: true })
  },
  unmounted(el) {
    el.removeEventListener('scroll', () => {})
  }
}
