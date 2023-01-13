const defaultTheme = require('tailwindcss/defaultTheme')

const colors = require('tailwindcss/colors')

module.exports = {
  corePlugins: {
    preflight: true
  },
  future: {
    removeDeprecatedGapUtilities: true,
    purgeLayersByDefault: true,
    defaultLineHeights: true,
    standardFontWeights: true
  },
  content: [
    "./src/main/resources/**/*.html",
    "./src/main/clojure/**/*.cljs"
  ],
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
    typography: {
      default: {
        css: {
          pre: {
            color: false,
            backgroundColor: false
          },
          a: {
            fontWeight: '500',
            textDecoration: 'underline',
            color: colors.blue['500']
          },
        },
      }
    }
  },
  plugins: [
    require('@tailwindcss/typography')
  ]
}
