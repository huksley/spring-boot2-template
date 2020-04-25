// https://stackoverflow.com/questions/3426404/create-a-hexadecimal-colour-based-on-a-string-with-javascript
function hashCode(str) {
  if (str === null || str === undefined || str === "") {
    return -1;
  }
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
export function TextColor(c) {
  let rgb = [c.substring(1, 3), c.substring(3, 5), c.substring(5, 7)];
  let o = Math.round(
    (parseInt(rgb[0], 16) * 299 +
      parseInt(rgb[1], 16) * 587 +
      parseInt(rgb[2], 16) * 114) /
      1000
  );
  return o > 125 ? "black" : "white";
}

export function StringColor(s) {
  return intToRGB(hashCode(s));
}
