function animate(str, primary, accent){
    primary = animate.color(primary);
	accent = animate.color(accent);
    
	var strs = [];
	
    if(str.length % 2 == 0){
        var mid = str.length/2;
        for(var i = 0; i < mid; i++){
            var temp = primary;
            temp += str.substring(mid - i, mid + 1 + i);
            strs.push(temp);
        }
    }else{
        var mid = parseInt(str.length/2);
        for(var i = 0; i < mid; i++){
            var temp = primary;
            temp += primary+str.substring(mid - i, mid + i);
            
            strs.push(temp);
        }
    }
	
	strs.push(primary+str);
	strs.push(accent+str);
    strs.push(primary+str);
    strs.push(accent+str);
    strs.push(primary+str);
    strs.push(accent+str);
	
	//finally, just return the array
	return strs;
}

animate.color = function(str){//a simple function to convert color code
	var ChatColor = Java.type('org.bukkit.ChatColor');
	return ChatColor.translateAlternateColorCodes('&', str);
}