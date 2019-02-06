// https://stackoverflow.com/questions/3426404/create-a-hexadecimal-colour-based-on-a-string-with-javascript
function hashCode(str) {
  // java String#hashCode
  var hash = 0;
  for (var i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  return hash;
}

function intToRGB(i) {
  var c = (i & 0x00ffffff).toString(16).toUpperCase();

  return "#" + ("00000".substring(0, 6 - c.length) + c);
}

// for color #FFFFFF make reverse color
// FIXME: not working 24, 1, 34 -> black???!?
export function TextColor(c) {
  let rgb = [ parseInt(c.substring(1, 2), 16), parseInt(c.substring(3, 2), 16), parseInt(c.substring(5, 2), 16) ]
  let o = Math.round(
    (parseInt(rgb[0]) * 299 + parseInt(rgb[1]) * 587 + parseInt(rgb[2]) * 114) /
      1000
  );
  return o > 150 ? "black" : "white";
}

export function StringColor(s) {
  return intToRGB(hashCode(s));
}
