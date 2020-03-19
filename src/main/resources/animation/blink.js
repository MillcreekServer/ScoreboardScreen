function animate(str, color){
    color = animate.color(color);
    
    var stripped = animate.stript(str);
    
	var strs = [];
	
    strs.push(color+stripped);
    strs.push(str);
	
	//finally, just return the array
	return strs;
}

animate.color = function(str){//a simple function to convert color code
	var ChatColor = Java.type('org.bukkit.ChatColor');
	return ChatColor.translateAlternateColorCodes('&', str);
}

animate.stript = function(str){
    var ChatColor = Java.type('org.bukkit.ChatColor');
	return ChatColor.stripColor(str);
}