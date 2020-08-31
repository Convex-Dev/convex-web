module.exports = {
  corePlugins: {
    preflight: true
  },
  future: {
      removeDeprecatedGapUtilities: true,
    },
  theme: {
    fontFamily: {
      sans: [
        'Roboto',
        'system-ui',
        '-apple-system',
        'BlinkMacSystemFont',
        '"Segoe UI"',
        '"Helvetica Neue"',
        'Arial',
        '"Noto Sans"',
        'sans-serif',
        '"Apple Color Emoji"',
        '"Segoe UI Emoji"',
        '"Segoe UI Symbol"',
        '"Noto Color Emoji"'
      ],
      rubik: ['Rubik']
    },
    extend: {},
  },
  variants: {},
  plugins: [
    require('@tailwindcss/typography')
  ]
}
