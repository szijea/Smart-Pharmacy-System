module.exports = {
  content: [
    './src/main/resources/static/**/*.html',
    './src/main/resources/static/js/**/*.js'
  ],
  theme: {
    extend: {
      colors: {
        primary: '#165DFF',
        secondary: '#36B37E',
        accent: '#FF5630',
        warning: '#FFAB00'
      },
      fontFamily: {
        inter: ['Inter','system-ui','sans-serif']
      }
    }
  },
  plugins: []
};
