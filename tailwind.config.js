const defaultTheme = require('tailwindcss/defaultTheme')

module.exports = {
  corePlugins: {
    preflight: true
  },
  future: {
    removeDeprecatedGapUtilities: true,
  },
  theme: {
    extend: {
      fontFamily: {
        sans: [
          'Inter',
          ...defaultTheme.fontFamily.sans,
        ],

        mono: [
          'Space Mono',
          ...defaultTheme.fontFamily.mono,
        ]
      }
    },
  },
  variants: {},
  plugins: [
    require('@tailwindcss/typography')
  ]
}
