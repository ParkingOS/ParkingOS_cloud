
var   kes = [
		"563031d4067bd699cce2a025", "5630314c4aa5720488ebd137",
		"5630318e067bd699cce2a01a", "563031594aa5720488ebd139",
		"563031d84aa5720488ebd14d", "563031a8067bd699cce2a01e",
		"56303190b0aa7963cf656d0e", "56302d66067bd699cce29f82",
		"563031394aa5720488ebd134", "563031d0b0aa7963cf656d18",
		"56302ca5b0aa7963cf656c57", "563031a54aa5720488ebd145",
		"5630321a067bd699cce2a030", "56302c5fb0aa7963cf656c4c",
		"56302bf5067bd699cce29f48", "56302cd3067bd699cce29f6b",
		"56302ea0067bd699cce29faf", "56302e30067bd699cce29fa2",
		"5630318b4aa5720488ebd141", "563031784aa5720488ebd13e" ];

function getzldtoken(input,index) {
	index = parseInt(index);
	input = input +kes[index].substring(15);
	
	var keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
	var output = "";
	var chr1, chr2, chr3 = "";
	var enc1, enc2, enc3, enc4 = "";
	var i = 0;
	do {
		chr1 = input.charCodeAt(i++);
		chr2 = input.charCodeAt(i++);
		chr3 = input.charCodeAt(i++);
		enc1 = chr1 >> 2;
		enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
		enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
		enc4 = chr3 & 63;
		if (isNaN(chr2)) {
		    enc3 = enc4 = 64;
		} else if (isNaN(chr3)) {
		    enc4 = 64;
		}
		output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2)
		                + keyStr.charAt(enc3) + keyStr.charAt(enc4);
		chr1 = chr2 = chr3 = "";
		enc1 = enc2 = enc3 = enc4 = "";
	} while (i < input.length);
	try{
		output=encode_61(output)
	}catch(e){
		
	}
	if(index>15)
		index=15;
	return kes[index].substring(index)+"==";
}

function encode_61(input) {
	var keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
	var output = "";
	var chr1, chr2, chr3 = "";
	var enc1, enc2, enc3, enc4 = "";
	var i = 0;
	do {
		chr1 = input.charCodeAt(i++);
		chr2 = input.charCodeAt(i++);
		chr3 = input.charCodeAt(i++);
		enc1 = chr1 >> 2;
		enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
		enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
		enc4 = chr3 & 63;
		if (isNaN(chr2)) {
		    enc3 = enc4 = 64;
		} else if (isNaN(chr3)) {
		    enc4 = 64;
		}
		output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2)
		                + keyStr.charAt(enc3) + keyStr.charAt(enc4);
		chr1 = chr2 = chr3 = "";
		enc1 = enc2 = enc3 = enc4 = "";
	} while (i < input.length);
	return output;
}