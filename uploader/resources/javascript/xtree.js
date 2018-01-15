//******************************************************************************************
//模块名称:可分步加载树型控件
//需要包含:none
//bug:资源有可能不能完全释放
//    在删除节点时，tree.allNodes集合的关键字无法删除，只是把元素设置为null
//******************************************************************************************
//所有树的集合
var _mtrees = new Array();
//******************************************************************************************
//树对象定义
//******************************************************************************************
var _baseDir = Base.template + "images/xtree/";
//var _baseDir="";
var treeLineI=_baseDir+"I.png";
var treeLineBlank=_baseDir+"blank.png";
var treeLineL=_baseDir+"L.png";
var treeLineLPlus=_baseDir+"Lplus.png";
var treeLineLMinus=_baseDir+"Lminus.png";
var treeLineT=_baseDir+"T.png";
var treeLineTPlus=_baseDir+"Tplus.png";
var treeLineTMinus=_baseDir+"Tminus.png";

function XTree(divObj){
	/**********节点图标样式集合**********/
	this.nodeStyles=new Array();
	//加载默认样式
	//this.nodeStyles["normal"]=new NodeStyle(_baseDir+"book.gif", _baseDir+"bookopen.gif");
	this.nodeStyles["normal"]=new NodeStyle(_baseDir + "foldericon.gif", _baseDir + "openfoldericon.gif");
	
	this.nodeStyles["leaf"]=new NodeStyle(_baseDir+"leaf.gif");

	/**********节点图标大小**********/
	this.iconWidth="16px";
	this.iconHeight="16px";

	/**********节点的不同状态下的显示样式**********/
	//正常状态
	this.normalNodeStyle=new Object();
	this.normalNodeStyle.color="black";
	this.normalNodeStyle.backgroundColor="transparent";
	this.normalNodeStyle.fontWeight="normal";
	this.normalNodeStyle.textDecorationUnderline=false;
	//选中状态
	this.selectedNodeStyle=new Object();
	this.selectedNodeStyle.color="white";
	this.selectedNodeStyle.backgroundColor="#08246B";
	this.selectedNodeStyle.fontWeight="bold";
	this.selectedNodeStyle.textDecorationUnderline=false;
	//悬挂状态
	this.hoverNodeStyle=new Object();
	this.hoverNodeStyle.color="#0000FF";
	this.hoverNodeStyle.backgroundColor="transparent";
	this.hoverNodeStyle.fontWeight="normal";
	this.hoverNodeStyle.textDecorationUnderline=true;

	/**********成员变量**********/
	this.key=null;						//树对象的关键字,唯一,自动生成
	this.container=divObj;				//树的容器，是<DIV>元素
	this.rootNodes =new Array();		// 根节点集合，没有关键字，按索引排列
	this.allNodes = {};					// 所有节点集合，关键字是节点Key
	this.selectedNode=null;				// 当前选择的节点

	/**********属性变量**********/
	this.showCheckBox = true;			//是否显示选择框
	this.checkBoxName = "ids";		//选择框的名称
	this.expandOnClick=false;			//是否单击展开节点,否则就双击展开
	this.hotTrack=false;				//是否使用热跟踪模式,即是否显示悬挂样式
	this.loadAllNodesOnce=false;		//是否一次性加载所有节点
	//默认的节点文本样式
	this.defaultTextStyle="PADDING-LEFT: 2px; FONT-SIZE: 12	px; FONT-FAMILY: Verdana; font-color: black";

	/**********方法申明**********/
	this.initialize=_initialize;		//初始化树
	this.addNode=_addNode;				//添加节点
	this.addRootNode=_addRootNode;		//添加根节点
	this.addNodesFromDOM=_addNodesFromDOM;	//从DOM对象添加子节点
	this.addNodesFromXML=_addNodesFromXML;	//从XML字符串添加子节点
	this.addNodesFromURL=_addNodesFromURL;	//从URL或文件添加子节点
	this.deleteNode=_deleteNode;		//删除节点
	this.redraw=_redraw;				//重画树的所有节点,在一次加载模式下,节点不是按层次的先后加载时,树的结构线会出错,必须调用这个方法重画
	this.redrawBranch=_redrawBranch;	//重画树的一个分支,当删除一个节点(该节点是该层的最后一个节点)后,需要调用本方法刷新它的上一个兄弟节点
	this.getNode=_getNode;				//根据关键字返回Node对象
	this.travelFirst=_travelFirst;		//先序遍历树的所有节点
	this.travelLast=_travelLast;		//后序遍历树的所有节点
	this.selectNode=_selectNode;		//选择一个节点
	this.getCheckedCount = _getCheckedCount;	// 得到被选择的节点的数量
	this.getCheckedNodes = _getCheckedNodes;	// 得到被选择的节点

	/**********事件申明**********/
	this.onLoadChildNodes=null;			//加载子节点，每个节点只会产生一次
	this.onCheckNode=null;				//当有选择框时，选择一个节点时产生
	this.onClickNode=null;				//点击节点
	this.onDblClickNode=null;			//双击节点
	this.onBeforeExpandNode=null;		//展开节点前，用户点击时才产生，程序调用不产生
	this.onBeforeSelect = null;			//选择节点前
	this.onExpandNode=null;				//展开节点后，用户点击时才产生，程序调用不产生
	this.onBeforeCollapseNode=null;		//收回节点前，用户点击时才产生，程序调用不产生
	this.onCollapseNode=null;			//收回节点后，用户点击时才产生，程序调用不产生
	
	
	
	this.destroy = _destroy;			//清空树
	this.unSelect = _unSelect;			//清空选中
	function _destroy(blnRefresh, treeSort) {
		for (var i = 0, l = this.rootNodes.length; i < l; i++) {
			if (Object.isUndefined(treeSort) || i === treeSort) {
				// 删除节点时数组中已经删除了元素，即每次循环都改变了数组.因此每次都调用下标为0的数组
				this.rootNodes[Object.isUndefined(treeSort) ? 0 : i].destroy(blnRefresh);
			}
		}
	}
	
	
	function _unSelect(){
		if (this.selectedNode!=null){
			_applyStyle(this.selectedNode.textObj, this.normalNodeStyle);
		}
		this.selectedNode = null;
	}

	/**********方法实现**********/
	//在指定的父节点下添加子节点，返回新增的节点
	//strParentKey: 父节点Key,""代表虚拟根
	//strChildKey: 要添加的子节点的Key
	//strText: 节点的文本
	//blnIsLeaf: 是不是叶节点,即末级节点
	//strStyleName: 该节点使用的样式,该样式定义在this.nodeStyles集合中
	function _addNode(strParentKey, strChildKey, strText, blnIsLeaf, strStyleName){
		//默认是非末级节点
		if (blnIsLeaf==null) blnIsLeaf=false;
		//默认样式,末级节点为leaf,其他为normal
		if (strStyleName==null || strStyleName=="")
			strStyleName=(blnIsLeaf?"leaf":"normal");
		//根据父节点调用对应的方法
		if (strParentKey==""){
			return(this.addRootNode(strChildKey, strText, blnIsLeaf, strStyleName));
		}
		else{
			var parentNode=this.allNodes[strParentKey];
			if (parentNode!=null){
				return(parentNode.addChildNode(strChildKey, strText, blnIsLeaf, strStyleName));
			}
		}
	}
	
	function _getCheckedCount() {
		var count = 0;
		for (var i = 0, l = this.rootNodes.length; i < l; i++) {
			count += _getCheckedCountByNode(this.rootNodes[i]);
		}
		return count;
	}
	
	function _getCheckedNodes() {
		var nodes = new Array();
		for (var key in this.allNodes) {
			var node = this.allNodes[key];
			if (node && node.getChecked()) {
				nodes.push(node);
			}
		}
		return nodes;
	}
	
	function _getCheckedCountByNode(node) {
		var count = 0;
		if (node.getChecked()) {
			count++;
		}
		
		for (var i = 0, l = node.childNodes.length; i < l; i++) {
			count += _getCheckedCountByNode(node.childNodes[i]);
		}
		return count;
	}

	//添加根节点，返回新增的节点
	function _addRootNode(strChildKey, strText, blnIsLeaf, strStyleName, nodeAttributes){
		//默认是非末级节点
		if (blnIsLeaf==null) blnIsLeaf=false;
		//默认样式,末级节点为leaf,其他为normal
		if (strStyleName==null || strStyleName=="")
			strStyleName=(blnIsLeaf?"leaf":"normal");
		var node=new XTreeNode();
		node.tree=this;
		node.attributes = nodeAttributes;
		node.parentNode=null;
		if (node.create(strChildKey, strText, blnIsLeaf, strStyleName)){
			node.setVisible(true);
			return(node);
		}
		else{
			return(null);
		}
	}

	//从domNode对象添加子节点,文本节点的值需要解码
	function _addNodesFromDOM(domNode){
		if (domNode==null) return(0);
		var xmlNodes=domNode.childNodes;
		var xmlNode=null;
		var lngCount=0;
		while((xmlNode=xmlNodes.nextNode())!=null){
			if (xmlNode.nodeName=="node"){
				//添加子节点
				var attr=null;
				var attributes = xmlNode.attributes;
				var nodeAttributes = {};
				var name = null;
				var value = null;
				var key = null;
				var text = null;
				var isleaf = false;
				var style = null;
				var onload = null;
				
				var b = this.showCheckBox;
				var j = 0;
				for (var i = 0, l = attributes.length; i < l; i++) {
					//遍历节点属性
					name = new String(attributes[i].name).trim();
					value = new String(attributes[i].value).trim();
					if (name.toLowerCase() == "properties") {
						var properties = StringHelper.evalJSON(value);
						if (properties) {
							for (var p in properties) {
								nodeAttributes[j++] = nodeAttributes[p] = properties[p];
							}
						}
						continue;
					}
					nodeAttributes[j++] = nodeAttributes[name] = value;
					name = name.toLowerCase();
					if (name == "key") {
						key = unescape(value);
					} else if (name == "text") {
						text = unescape(value);
					} else if (name == "leaf") {
						// value = value.toLowerCase();
						isleaf = (StringHelper.isTrue(value)) ? true : false;
					} else if (name == "style") {
						style = value;
					} else if (name == "onload") {
						onload = value;
					}
				}
				
				/*var key=unescape(attributes.getNamedItem("key").text);
				var text=unescape(attributes.getNamedItem("text").text);
				attr=attributes.getNamedItem("isleaf");
				var isleaf=(attr!=null&&attr.text=="1"?true:false);
				attr=attributes.getNamedItem("style");
				var style=(attr==null?"":attr.text);
				//alert(key+" "+text+" "+isleaf+" "+style);
				*/
				//判断是否有重复关键字
				if (this.allNodes[key] != null) {
					continue;
				}
				
				// this.showCheckBox = (b === 1 || (b && canDelete));
				var node = this.addRootNode(key, text, isleaf, style, nodeAttributes);
				lngCount++;
				this.showCheckBox = b;
				
				//执行节点加载后的事件
				//attr = attributes.getNamedItem("onload");
				if (onload != null && onload != "") {
					var onloadHandle = new Function("node", unescape(onload));
					onloadHandle(node);
				}
				
				//添加下级子节点
				var childXmlNodes = xmlNode.childNodes;
				var childXmlNode = null;
				while((childXmlNode = childXmlNodes.nextNode()) != null){
					name = new String(childXmlNode.nodeName).trim();
					if (name.toLowerCase() == "nodes"){
						lngCount += node.addNodesFromDOM(childXmlNode);
					} else {
						node.setAttribute(name, childXmlNode.text);
					}
				}
			}
		}
		this.redraw();
		return(lngCount);
	}

	//从XML字符串添加子节点
	function _addNodesFromXML(xml){
		var doc = new ActiveXObject("Microsoft.XMLDOM");
		doc.async=false;
		doc.loadXML(xml);
		return(this.addNodesFromDOM(doc.documentElement));
	}

	//从URL或文件添加子节点
	function _addNodesFromURL(url){
		var doc = new ActiveXObject("Microsoft.XMLDOM");
		doc.async=false;
		doc.load(url);
		return(this.addNodesFromDOM(doc.documentElement));
	}

	//删除节点
	function _deleteNode(strNodeKey, blnRefresh){
		var node=this.allNodes[strNodeKey];
		if (node!=null)
			node.destroy(blnRefresh==null?true:blnRefresh);
	}

	//初始化
	function _initialize(){
		this.container.onselectstart=_cancelEvent;	//不能选中
		this.container.ondragstart=_cancelEvent;
		//设置关键字,方便查找
		this.key="t"+Math.round(Math.random()*50000);
		this.container.treeKey=this.key;
		_mtrees[this.key]=this;
		
	}

	//重画树的所有节点
	function _redraw(){
		var nodes = this.rootNodes;
		for(var i = 0, l = nodes.length; i < l; i++) {
			nodes[i].redraw();
		}
	}

	//重画树的一个分支
	function _redrawBranch(strNodeKey){
		var node=this.allNodes[strNodeKey];
		if (node!=null)
			node.redrawBranch();
	}

	//先序遍历树,strCallbackFun是回调函数,可以使用node对象代表当前访问的节点
	function _travelFirst(strCallbackFun){
		for (var i=0;i<this.rootNodes.length;i++){
			this.rootNodes[i].travelFirst(strCallbackFun);
		}
	}

	//后序遍历树,strCallbackFun是回调函数,可以使用node对象代表当前访问的节点
	function _travelLast(strCallbackFun){
		for (var i=0;i<this.rootNodes.length;i++){
			this.rootNodes[i].travelLast(strCallbackFun);
		}
	}

	//选择一个节点
	function _selectNode(strNodeKey){
		var node=this.allNodes[strNodeKey];
		if (node!=null)
			node.select();
	}

	//根据关键字返回Node对象
	function _getNode(strKey){
		return(this.allNodes[strKey]);
	}

	//取消事件
	function _cancelEvent(){
		return(false);
	}

	//进行初始化
	this.initialize();
}


//******************************************************************************************
//节点对象定义
//******************************************************************************************
/*
	节点HTML结构说明:
	<div>------------节点的容器
	<table><tr>-----------------本节点的HTML元素
	<td>数的结构线</td>
	<td>节点前的+-号图标</td>
	<td>节点图标,可以表示打开和关闭</td>
	<td>节点选择框,可选</td>
	<td>节点的文字</td>
	</tr></table>
	<div>----------------------下级节点，结构同本节点
	...
	</div>

	</div>
*/
function XTreeNode(){
	/**********成员变量和属性**********/
	this.tree=null;							//所在的树对象
	this.parentNode=null;					//父节点对象
	this.childNodes = new Array();			//子节点集合，没有关键字，按索引排列
	this.container=null;					//节点的容器,是<DIV>元素
	this.textObj=null;						//节点的文本元素,是<TD>
	this.signObj=null;						//节点的标记元素,代表节点是否展开,+-号图标,是<IMG>
	this.iconObj=null;						//节点的图标元素,是<IMG>
	this.checkBox=null;						//节点的复选框对象
	this.key=null;							//本节点的关键字
	this.text=null;							// 显示的文本
	this.isChildNodesLoaded=false;			//是否已经加载了下级节点，false时当打开节点时会产生onLoadChildNodes事件
	this.isOpen=false;						//节点是否打开
	this.isLeaf=false;						//是不是末级节点
	this.nodeStyle=null;					//节点的图形样式
	
	this.attributes = {};			//节点的属性对象

	/**********方法申明**********/
	this.create=_create;					//初始化节点,内部调用
	this.destroy=_destroy;					//删除本节点,内部调用
	this.redraw=_redraw;					//重画节点,包括结构线,标记,图标
	this.redrawBranch=_redrawBranch;		//重画节点所在分支,包括结构线,标记,图标
	this.addChildNode=_addChildNode;		//添加子节点
	this.addNodesFromDOM=_addNodesFromDOM;	//从DOM对象添加子节点
	this.addNodesFromXML=_addNodesFromXML;	//从XML字符串添加子节点
	this.addNodesFromURL=_addNodesFromURL;	//从URL或文件添加子节点
	this.deleteChildNodes=_deleteChildNodes;	//删除子节点,同时刷新本节点
	this.expand=_expand;					//展开节点
	this.collapse=_collapse;				//收回节点
	this.travelFirst=_travelFirst;			//先序遍历节点所在分支
	this.travelLast=_travelLast;			//后序遍历节点所在分支
	this.loadChildNodes=_loadChildNodes;	//产生onLoadChildNodes事件
	this.unloadChildNodes=_unloadChildNodes;	//卸载子节点,并设置标记,可以再次产生onLoadChildNodes事件
	this.reloadChildNodes=_reloadChildNodes;	//重新加载子节点,即先卸载节点,再调用loadChildNodes方法
	this.select=_select;					//选择本节点
	this.ensureVisible=_ensureVisible;		//展开节点并滚动树使节点可见

	this.getSiblingNodes=_getSiblingNodes;	//得到兄弟节点集合
	this.isLastChild=_isLastChild;			//是不是本层的最后一个子节点
	this.getFirstSibling=_getFirstSibling;	//得到第一个兄弟
	this.getLastSibling=_getLastSibling;	//得到最后一个兄弟
	this.getPrevSibling=_getPrevSibling;	//得到前一个兄弟
	this.getNextSibling=_getNextSibling;	//得到后一个兄弟

	this.getText=_getText;					//读取节点文本
	this.setText=_setText;					//设置节点文本
	this.getVisible=_getVisible;			//读取是否可见
	this.setVisible=_setVisible;			//设置是否可见
	this.getTitle=_getTitle;				//读取提示文本
	this.setTitle=_setTitle;				//设置提示文本
	this.getChecked=_getChecked;			//读取复选状态
	this.setChecked=_setChecked;			//设置复选状态
	
	this.getPath = _getPath;				// 得到路径.使用父节点 >> 节点的格式
	this.getAbsolutePath = function() {		// 得到从根节点开始到当前节点的路径,返回节点数组,第一个为根节点
		var nodes = [ this ];
		var node = this;
		while (node = node.parentNode) {
			nodes.splice(0, 0, node);
		}
		return nodes;
	};
	
	
	
	this.getAttribute = function(key) {
		return Object.isUndefined(this.attributes) || Object.isUndefined(this.attributes[key]) ? null : this.attributes[key];
	}
	
	this.setAttribute = function(key, value) {
		if (Object.isUndefined(this.attributes)) {
			this.attributes = {};
		}
		this.attributes[key] = value;
	}
	
	/* 
	 * time: 向上迭代的次数
	 * separator: 分隔符,默认为" >> "
	 */
	function _getPath(time, separator ) {
		var path = this.text;
		var node = this.parentNode;
		var t = 0;
		// 默认显示全路径
		if (!NumberHelper.isNumber(time)) {
			separator = time;
			time = -1;
		} else {
			time = NumberHelper.intValue(time, -1);
		}
		separator = Object.isUndefined(separator) ? " > " : separator;
		// alert('separator = ' + separator);
		while (node && (time < 0 || time > t)) {
			path = node.text + separator + path;
			node = node.parentNode;
		}
		return path;
	}

	/**********方法实现**********/
	//添加子节点，返回新增的节点
	function _addChildNode(strChildKey, strText, blnIsLeaf, strStyleName, attributes){
		//默认是非末级节点
		if (blnIsLeaf==null) blnIsLeaf=false;
		//默认样式,末级节点为leaf,其他为normal
		if (strStyleName==null || strStyleName=="")
			strStyleName=(blnIsLeaf ? "leaf" : "normal");
		var node = new XTreeNode();
		node.tree = this.tree;
		node.parentNode = this;
		node.attributes = attributes;
		if (node.create(strChildKey, strText, blnIsLeaf, strStyleName))
			return(node);
		else
			return(null);
	}

	//从domNode对象添加子节点,文本节点的值需要解码
	function _addNodesFromDOM(domNode){
		if (domNode==null) return(0);
		var xmlNodes = domNode.childNodes;
		if (xmlNodes == null || xmlNodes.length <= 0) return(0);
		var xmlNode=null;
		var lngCount=0;
		while((xmlNode=xmlNodes.nextNode())!=null){
			if (xmlNode.nodeName=="node"){
				//添加子节点
				var attr=null;
				var attributes = xmlNode.attributes;
				var nodeAttributes = {};
				var name = null;
				var value = null;
				var key = null;
				var text = null;
				var isleaf = false;
				var style = null;
				var onload = null;
				var canDelete = false;
				var b = this.tree.showCheckBox;
				var j = 0;
				for (var i = 0, l = attributes.length; i < l; i++) {
					//遍历节点属性
					name = new String(attributes[i].name).trim();
					value = new String(attributes[i].value).trim();
					if (name.toLowerCase() == "properties") {
						var properties = StringHelper.evalJSON(value);
						if (properties) {
							for (var p in properties) {
								nodeAttributes[j++] = nodeAttributes[p] = properties[p];
							}
						}
						continue;
					}
					nodeAttributes[j++] = nodeAttributes[name] = value;
					name = name.toLowerCase();
					if (name == "key") {
						key = unescape(value);
					} else if (name == "text") {
						text = unescape(value);
					} else if (name == "leaf") {
						//value = value.toLowerCase();
						isleaf = (StringHelper.isTrue(value)) ? true : false;
					} else if (name == "style") {
						style = value;
					} else if (name == "onload") {
						onload = value;
					}
				}
				
				/*var key=unescape(attributes.getNamedItem("key").text);
				var text=unescape(attributes.getNamedItem("text").text);
				attr=attributes.getNamedItem("isleaf");
				var isleaf=(attr!=null&&attr.text=="1"?true:false);
				attr=attributes.getNamedItem("style");
				var style=(attr==null?"":attr.text);
				//alert(key+" "+text+" "+isleaf+" "+style);
				*/
				//判断是否有重复关键字
				if (this.tree.allNodes[key] != null) {
					continue;
				}
				
				// this.tree.showCheckBox = 1; // (b === 1 || (b && canDelete));
				var node = this.addChildNode(key, text, isleaf, style, nodeAttributes);
				lngCount++;
				this.tree.showCheckBox = b;
				
				//执行节点加载后的事件
				//attr = attributes.getNamedItem("onload");
				if (onload != null && onload != "") {
					var onloadHandle = new Function("node", unescape(onload));
					onloadHandle(node);
				}
				
				//添加下级子节点
				var childXmlNodes = xmlNode.childNodes;
				var childXmlNode = null;
				while((childXmlNode = childXmlNodes.nextNode()) != null){
					name = new String(childXmlNode.nodeName).trim();
					if (name.toLowerCase() == "nodes"){
						lngCount += node.addNodesFromDOM(childXmlNode);
					} else {
						node.setAttribute(name, childXmlNode.text);
					}
				}
			}
		}
		return(lngCount);
	}

	//从XML字符串添加子节点
	function _addNodesFromXML(xml){
		var doc = new ActiveXObject("Microsoft.XMLDOM");
		doc.async=false;
		doc.loadXML(xml);
		return(this.addNodesFromDOM(doc.documentElement));
	}

	//从URL或文件添加子节点
	function _addNodesFromURL(url){
		var doc = new ActiveXObject("Microsoft.XMLDOM");
		doc.async=false;
		doc.load(url);
		return(this.addNodesFromDOM(doc.documentElement));
	}

	//初始化
	function _create(strKey, strText, blnIsLeaf, strStyleName){
		//校验
		if (this.tree==null){
			alert("树对象不能为null!");
			return(false);
		}
		if (this.parentNode!=null && this.parentNode.isLeaf){
			alert("不能在末级节点下添加节点!");
			return(false);
		}
		//赋值
		this.key=strKey;
		this.text = strText;
		this.isLeaf=blnIsLeaf;
		this.nodeStyle=this.tree.nodeStyles[strStyleName];
		this.isChildNodesLoaded=(blnIsLeaf?true:this.tree.loadAllNodesOnce);
		//加入集合
		var siblingNodes=this.getSiblingNodes();
		this.tree.allNodes[strKey]=this;
		siblingNodes[siblingNodes.length]=this;
		//生成节点的HTML
		var strHTML=null;
		//生成树的结构线,生成依据:如果父节点是该层的最后一个节点那么就画竖线,否则就不画
		strHTML="<td id='tline' style='cursor:default'>"+_getTreeLineHTML(this)+"</td>";
		//生成本节点前的加减号图标
		strHTML=strHTML+"<td><img id='sign' height='"+this.tree.iconHeight+"' src='"+_getNodeSign(this)+"' onclick='_clickOnSign(this);'></td>";
		//生成节点图标
		strHTML=strHTML+"<td valign=center><img id='icon' width='"+this.tree.iconWidth+"' height='"+this.tree.iconHeight+"' src='"+this.nodeStyle.image+"' onclick='_clickOnSign(this);'></td>";
		//生成复选框
		if (this.tree.showCheckBox){
			strHTML=strHTML+"<td valign=center><input type=checkbox onpropertychange='_clickOnChkBox(this)' onclick='_clickOnChkBox(this)' name='"
					+ this.tree.checkBoxName + "' id='" + this.tree.checkBoxName + "' value='" + this.key + "' style='height:12px'></td>";
		}
		//生成节点文本
		strHTML=strHTML+"<td id='text' style='"+this.tree.defaultTextStyle+"' "+(this.tree.hotTrack?"onmouseover='_enterNode(this)' onmouseout='_leaveNode(this)'":"")+" onclick='_clickOnText(this)' ondblclick='_dblClickOnText(this)' valign=center nowrap>"+_HTMLEncode(strText+"")+"</td>";
		//插入节点
		var nid=document.uniqueID;
		var strVisible=(this.parentNode!=null&&!this.parentNode.isOpen?"none":"block");
		strHTML="<div id='"+nid+"' style='display:"+strVisible+";cursor:hand' onclick='window.event.cancelBubble=true; '><table id='tbl' cellspacing=0 cellpadding=0 border=0><tr>"+strHTML+"</tr></table></div>";
		//alert(strHTML);
		if (this.parentNode==null){
			//添加根节点
			this.tree.container.insertAdjacentHTML("BeforeEnd",strHTML);
		}
		else{
			//添加子节点
			this.parentNode.container.insertAdjacentHTML("BeforeEnd",strHTML);
		}
		//设置相关对象
		this.container=document.all(nid);
		this.signObj=this.container.all["sign"];
		this.iconObj=this.container.all["icon"];
		this.textObj=this.container.all["text"];
		if (this.tree.showCheckBox){
			this.checkBox=this.container.all[this.tree.checkBoxName];
		} else {
			this.checkBox = null;
		}
		this.container.treeKey=this.tree.key;			//树关键字
		this.container.nodeKey=this.key;				//节点关键字
		//重画上一个兄弟节点
		if (siblingNodes.length>1){
			var prevNode=siblingNodes[siblingNodes.length-2];
			prevNode.signObj.src=_getNodeSign(prevNode);
		}
		//重画父节点
		if (this.parentNode!=null && siblingNodes.length==1){
			this.parentNode.signObj.src=_getNodeSign(this.parentNode);
		}
		return(true);
	}

	//删除本节点
	//blnRefresh:是否刷新节点
	//返回已删除的节点数
	function _destroy(blnRefresh){
		//已删除的节点数
		var lngCount=1;
		//如果要刷新节点，就判断是不是本级的最后一个节点
		if (blnRefresh==null) blnRefresh=false;
		var isLastNode=false;
		if (blnRefresh){
			isLastNode=this.isLastChild();
		}
		//先删除下级节点
		while(this.childNodes.length>0){
			lngCount+=this.childNodes[0].destroy(false);
		}
		//从集合中删除
		this.tree.allNodes[this.key]=null;
		var siblingNodes=this.getSiblingNodes();
		//alert("sibling count:"+siblingNodes.length);
		for (var i=0;i<siblingNodes.length;i++){
			if (siblingNodes[i]==this){
				//删除
				siblingNodes.splice(i,1);
				break;
			}
		}
		//如果是最后一个节点，就刷新父节点
		if (siblingNodes.length==0 && this.parentNode!=null)
			this.parentNode.signObj.src=_getNodeSign(this.parentNode);
		//刷新上个兄弟节点分支
		if (blnRefresh && isLastNode && siblingNodes.length>0){
			siblingNodes[siblingNodes.length-1].redrawBranch();
		}
		//处理当前节点
		if (this.tree.selectedNode==this){
			this.tree.selectedNode=null;
		}
		//最后删除本节点
		this.container.outerHTML="";
		this.container=null;
		this.tree=null;
		this.parentNode=null;
		this.container=null;
		this.textObj=null;
		this.signObj=null;
		this.iconObj=null;
		this.checkBox=null;
		this.nodeStyle=null;
		this.childNodes=null;
		//返回
		return(lngCount);
	}

	//删除子节点,同时刷新本节点
	function _deleteChildNodes(){
		var lngCount=0;
		for (var i=this.childNodes.length-1; i>=0; i--){
			lngCount+=this.childNodes[i].destroy(false);
		}
		return(lngCount);
	}

	//删除子节点,并设置标记,可以再次产生onLoadChildNodes事件
	function _unloadChildNodes(){
		var lngCount=this.deleteChildNodes();
		this.isChildNodesLoaded=(this.isLeaf?true:this.tree.loadAllNodesOnce);
		this.isOpen=false;
		this.redraw();
		return(lngCount);
	}

	//删除子节点,并设置标记,可以再次产生onLoadChildNodes事件
	function _reloadChildNodes(){
		var isOpen=this.isOpen;
		var lngCount=this.unloadChildNodes();
		this.loadChildNodes();
		if (isOpen){
			this.expand();
		}
		else{
			this.collapse();
		}
		return(lngCount);
	}

	//选择本节点
	function _select(){
		if (this.tree.selectedNode!=null){
			_applyStyle(this.tree.selectedNode.textObj, this.tree.normalNodeStyle);
			if (this.tree.onBeforeSelect) {
				this.tree.onBeforeSelect(this.tree.selectedNode);
			}
		}
		this.tree.selectedNode=this;
		_applyStyle(this.textObj, this.tree.selectedNodeStyle);
	}

	//展开节点并滚动树使节点可见
	function _ensureVisible(){
		var node=this.parentNode;
		while(node!=null){
			if (!node.isOpen) node.expand();
			node=node.parentNode;
		}
		this.textObj.scrollIntoView();
	}

	//是否可见
	function _setVisible(blnVisible){
		this.container.style.display=(blnVisible?"block":"none");
	}

	function _getVisible(){
		return(this.container.style.display=="none"?false:true);
	}

	//正文
	function _setText(strText){
		this.textObj.innerText=strText;
	}

	function _getText(){
		return(this.textObj.innerText);
	}

	//提示文本
	function _setTitle(strTitle){
		this.textObj.title=strTitle;
	}

	function _getTitle(){
		return(this.textObj.title);
	}

	//复选状态
	function _setChecked(blnChecked){
		this.checkBox.checked=blnChecked;
	}

	function _getChecked(){
		return(this.checkBox && this.checkBox.checked);
	}

	//展开下一级节点
	function _expand(){
		if (this.isLeaf) return;
		var nodes=this.childNodes;
		this.isOpen=true;
		this.iconObj.src=this.nodeStyle.imageOpen;
		this.signObj.src=_getNodeSign(this);
		for (var i=0; i<nodes.length; i++){
			nodes[i].setVisible(true);
		}
	}

	//收回下一级节点
	function _collapse(){
		if (this.isLeaf) return;
		var nodes=this.childNodes;
		this.isOpen=false;
		this.iconObj.src=this.nodeStyle.image;
		this.signObj.src=_getNodeSign(this);
		for (var i=0; i<nodes.length; i++){
			nodes[i].setVisible(false);
		}
	}

	//重画本节点
	function _redraw(){
		this.container.children["tbl"].all["tline"].innerHTML=_getTreeLineHTML(this);
		this.signObj.src=_getNodeSign(this);
		this.iconObj.src=(this.isOpen?this.nodeStyle.imageOpen:this.nodeStyle.image);
		
		//重画其子节点
		var nodes = this.childNodes;
		for (var i = 0, l = nodes.length; i < l; i++) {
			nodes[i].redraw();
		}
	}

	//重画本节点所在的分支
	function _redrawBranch(){
		this.redraw();
		for (var i=0;i<this.childNodes.length;i++){
			this.childNodes[i].redrawBranch();
		}
	}

	//得到本节点所在的兄弟节点集合
	function _getSiblingNodes(){
		if (this.parentNode==null)
			return(this.tree.rootNodes);
		else{
			return(this.parentNode.childNodes);
		}
	}

	//是不是本层的最后一个节点
	function _isLastChild(){
		var nodes=this.getSiblingNodes();
		return(nodes[nodes.length-1]==this);
	}

	//得到第一个兄弟
	function _getFirstSibling(){
		var nodes=this.getSiblingNodes();
		return(nodes.length>0?nodes[0]:null);
	}

	//得到最后一个兄弟
	function _getLastSibling(){
		var nodes=this.getSiblingNodes();
		return(nodes.length>0?nodes[nodes.length-1]:null);
	}

	//得到前一个兄弟
	function _getPrevSibling(){
		var nodes=this.getSiblingNodes();
		for (var i=0;i<nodes.length;i++){
			if (nodes[i]==this){
				return(i>0?nodes[i-1]:null);
			}
		}
	}

	//得到后一个兄弟
	function _getNextSibling(){
		var nodes=this.getSiblingNodes();
		for (var i=0;i<nodes.length;i++){
			if (nodes[i]==this){
				return(i+1<nodes.length?nodes[i+1]:null);
			}
		}
	}

	//产生onLoadChildNodes事件
	function _loadChildNodes(){
		_checkLoadChildsEvent(this);
	}

	//先序遍历节点所在分支,strCallbackFun是回调函数,可以使用node对象代表当前访问的节点
	function _travelFirst(strCallbackFun){
		var fun=new Function("node", strCallbackFun);
		fun(this);
		for (var i=0;i<this.childNodes.length;i++){
			this.childNodes[i].travelFirst(strCallbackFun);
		}
	}

	//后序遍历节点所在分支,strCallbackFun是回调函数,可以使用node对象代表当前访问的节点
	function _travelLast(strCallbackFun){
		for (var i=0;i<this.childNodes.length;i++){
			this.childNodes[i].travelLast(strCallbackFun);
		}
		var fun=new Function("node", strCallbackFun);
		fun(this);
	}
}

//******************************************************************************************
//节点样式对象定义
//******************************************************************************************
function NodeStyle(strImage, strImageOpen){
	if (strImageOpen==null || strImageOpen=="") strImageOpen=strImage;
	this.image=strImage;
	this.imageOpen=strImageOpen;
}

//******************************************************************************************
//辅助方法:事件处理函数
//******************************************************************************************
function _clickOnSignByNode(node) {
	if (node.isLeaf) return;
	_checkLoadChildsEvent(node);
	if (node.isOpen){
		//产生用户事件
		if (node.tree.onBeforeCollapseNode!=null){
			node.tree.onBeforeCollapseNode(node);
		}
		node.collapse();
		//产生用户事件
		if (node.tree.onCollapseNode!=null){
			node.tree.onCollapseNode(node);
		}
	}
	else{
		//产生用户事件
		if (node.tree.onBeforeExpandNode!=null){
			node.tree.onBeforeExpandNode(node);
		}
		node.expand();
		//产生用户事件
		if (node.tree.onExpandNode!=null){
			node.tree.onExpandNode(node);
		}
	}
}

//用户点击标记展开或者收回节点
function _clickOnSign(element){
	var node=_getNodeByDIV(_getParent(element,"div"));
	_clickOnSignByNode(node);
}

function _clickOnTextByNode(node) {
	//判断是否单击展开节点
	if (node.tree.expandOnClick){
		_clickOnSign(element);
	}
	//选中该节点
	node.select();
	//产生用户事件
	if (node.tree.onClickNode!=null){
		node.tree.onClickNode(node);
	}
}
//用户单击节点正文
function _clickOnText(element){
	var node=_getNodeByDIV(_getParent(element,"div"));
	_clickOnTextByNode(node);
}

//用户双击节点正文
function _dblClickOnText(element){
	var node=_getNodeByDIV(_getParent(element,"div"));
	//判断是否是双击展开节点
	if (!node.tree.expandOnClick){
		_clickOnSign(element);
	}
	//产生用户事件
	if (node.tree.onDblClickNode!=null){
		node.tree.onDblClickNode(node);
	}
}

//点击节点复选框
function _clickOnChkBox(element){
	var node=_getNodeByDIV(_getParent(element,"div"));
	//产生用户事件
	if (node.tree.onCheckNode!=null){
		node.tree.onCheckNode(node);
	}
}

//检查加载子节点事件
function _checkLoadChildsEvent(node){
	if (node.isChildNodesLoaded) return;
	//调用加载事件
	node.isChildNodesLoaded=true;
	if (node.tree.onLoadChildNodes!=null){
		node.tree.onLoadChildNodes(node);
	}
	//更新显示
	node.signObj.src=_getNodeSign(node);
}

//鼠标进入节点正文
function _enterNode(element){
	var node=_getNodeByDIV(_getParent(element,"div"));
	if (!node.tree.hotTrack) return;
	if (node.tree.selectedNode!=node)
		_applyStyle(node.textObj, node.tree.hoverNodeStyle);
}

//鼠标离开节点正文
function _leaveNode(element){
	var node=_getNodeByDIV(_getParent(element,"div"));
	if (!node.tree.hotTrack) return;
	_applyStyle(node.textObj, node.tree.selectedNode==node?node.tree.selectedNodeStyle:node.tree.normalNodeStyle);
}

//******************************************************************************************
//辅助方法:功能函数
//******************************************************************************************
//得到指定对象的指定类型的父对象
function _getParent(objChild,strParentType){
	var objParent=objChild;
	
	if (strParentType==null || objChild==null) return(null);
	strParentType=strParentType+"";
	do{
		objParent=objParent.parentElement;
	}while (objParent!=null && objParent.tagName.toUpperCase()!=strParentType.toUpperCase());
	return(objParent);
}

//应用样式到节点
function _applyStyle(element,nodeStyle){
	for (var name in nodeStyle){
		element.style[name]=nodeStyle[name];
	}
}

//得到节点的+-标记
function _getNodeSign(node){
	if (!node.isChildNodesLoaded){
		//分步加载时,除末级节点外每个节点都有+号
		if (node.isLastChild())
			return(node.isLeaf?treeLineL:treeLineLPlus);
		else
			return(node.isLeaf?treeLineT:treeLineTPlus);
	}
	else{
		if (node.childNodes.length==0){
			//没有子节点
			return(node.isLastChild()?treeLineL:treeLineT);
		}
		else{
			//有子节点
			if (node.isOpen)
				//打开状态
				return(node.isLastChild()?treeLineLMinus:treeLineTMinus);
			else
				//收回状态
				return(node.isLastChild()?treeLineLPlus:treeLineTPlus);
		}
	}
}

//得到树的结构线,生成依据:如果父节点是该层的最后一个节点那么就画竖线,否则就不画
function _getTreeLineHTML(node){
	var pnode=node.parentNode;
	var strLineHTML="";
	while(pnode!=null){
		var strImage=null;
		strLineHTML="<img height='"+node.tree.iconHeight+"' src='"+(pnode.isLastChild() ? treeLineBlank:treeLineI) + "'>"+strLineHTML;
		pnode=pnode.parentNode;
	}
	return(strLineHTML);
}

//通过节点的<DIV>元素找到Node
function _getNodeByDIV(divObj){
	var tree=_mtrees[divObj.treeKey];
	if (tree==null) return(null);
	return(tree.allNodes[divObj.nodeKey]);
}

//对字符串进行HTML编码转换
function _HTMLEncode(s){
	s = s.replace(/</g, "&lt;");
	s = s.replace(/>/g, "&gt;");
	s = s.replace(/ /g, "&nbsp;");
	s = s.replace(/'/g, "&#39;");
	s = s.replace(/"/g, "&quot;");
	//s = s.replace("\r\n", "&#13;");
	return(s);
}

//由于在页面中有双向引用,所以在卸载页面时要释放这些对象
function _releaseObjects(){
	for (var i in _mtrees){
		var tree=_mtrees[i];
		//清除树的引用
		tree.container=null;
		tree.selectedNode=null;
		//清除所有节点集合
		tree.allNodes=null;
		//释放节点
		var nodes=tree.rootNodes;
		for (var j = 0; nodes && j < nodes.length; j++){
			_releaseNode(nodes[j]);
		}
		tree.rootNodes=null;
	}
	for (var i in _mtrees){
		delete _mtrees[i];
	}
}

//释放节点,递归调用
function _releaseNode(node){
	//alert(node.key);
	node.tree=null;
	node.parentNode=null;
	node.container=null;
	node.textObj=null;
	node.signObj=null;
	node.iconObj=null;
	node.checkBox=null;
	node.nodeStyle=null;
	var nodes=node.childNodes;
	for (var i = 0; nodes && i<nodes.length; i++){
		_releaseNode(nodes[i]);
	}
	node.childNodes=null;
}

//卸载页面时释放对象
// window.attachEvent("onunload",_releaseObjects);
Event.observe(window, "unload", _releaseObjects);
