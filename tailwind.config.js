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
    fontFamily: {
      sans: ['Inter', 'sans-serif'],
      mono: ['Space Mono'],
      "source-sans-pro": ['Source Sans Pro', 'sans-serif']
    },
    extend: {
      typography: {
        DEFAULT: {
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
  },
  plugins: [
    require('@tailwindcss/typography')
  ]
}
