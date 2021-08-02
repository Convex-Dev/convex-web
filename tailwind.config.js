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
  purge: [
    './src/**/*.clj',
    './src/**/*.cljs'
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
      },
      colors: {
        teal: colors.teal
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
  variants: {},
  plugins: [
    require('@tailwindcss/typography'),
    require('@tailwindcss/ui')
  ]
}
