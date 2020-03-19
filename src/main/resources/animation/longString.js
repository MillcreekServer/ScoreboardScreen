function animate(str, max){
    max = Math.min(str.length, max);
    
	var strs = [];
	
    for(var i = 0; i < 5; i++)
        strs.push(str.substring(0, max));
    
	for(var i = 0; i < str.length - max; i++){
		strs.push(str.substring(i, i + max));
	}
    
    for(var i = 0; i < 5; i++)
        strs.push(str.substring(str.length - max));
	
	return strs;
}

animate.color = function(str){
	var ChatColor = Java.type('org.bukkit.ChatColor');
	return ChatColor.translateAlternateColorCodes('&', str);
}