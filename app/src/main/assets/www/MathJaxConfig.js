
  MathJax.Hub.Config({
  messageStyle: "none",
  /*styles: {
    "#MathJax_Message": {left: "", right: 0},
    "#MathJax_MSIE_Frame": {left: "", right: 0}
  },*/
  showProcessingMessages: false,
    tex2jax: { inlineMath: [['$','$'],['\\(','\\)']] },
    asciimath2jax: {
      delimiters: [['`','`'], ['$','$']]
    },
    "fast-preview": {
    Chunks: {EqnChunk: 10000, EqnChunkFactor: 1, EqnChunkDelay: 0},
    color: "inherit!important",
    updateTime: 30, updateDelay: 6,
    messageStyle: "none",
    disabled: false
  }, CommonHTML: { linebreaks: { automatic: true } }, "HTML-CSS": { linebreaks: { automatic: true } },SVG: { linebreaks: { automatic: true } }
  });