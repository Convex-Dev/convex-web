const defaultTheme = require('tailwindcss/defaultTheme')

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
        blue: {
          '100': '#E0F6FD',
          '200': '#C3EAFC',
          '300': '#A2D8F6',
          '400': '#88C3EC',
          '500': '#62A6E1',
          '600': '#4782C1',
          '700': '#3161A2',
          '800': '#1F4482',
          '900': '#122F6C',
        },
        indigo: {
          '100': '#DCEAFC',
          '200': '#BAD4FA',
          '300': '#94B7F0',
          '400': '#769BE2',
          '500': '#4B74CF',
          '600': '#3659B2',
          '700': '#254195',
          '800': '#172C78',
          '900': '#0E1D63',
        }
      }
    },
    typography: {
      default: {
        css: {
          pre: {
            color: false,
            backgroundColor: false
          }
        },
      },
    }
  },
  variants: {},
  plugins: [
    require('@tailwindcss/typography'),
    require('@tailwindcss/ui')
  ]
}
