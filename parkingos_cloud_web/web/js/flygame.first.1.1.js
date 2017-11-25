/*! complie time << dgc55344#163.com >> 2015-11-05_11-05-12 */this.tcb = this.tcb || {};
(function(){
    /**
     * 坐标点
     * @param x
     * @param y
     * @constructor
     */
    var Point = function(x,y){
        this.x = x || 0;
        this.y = y || 0;
    };
    Point.prototype.set = function(x,y){
        this.x = x || 0;
        this.y = y || 0;
    };
    tcb.Point = Point;
})();

(function(){
    var Callback = function(func,ctx){
        this.func = func || function(){};
        this.ctx = ctx || null;
    };
    Callback.prototype.c = function(p,angle,d){
        this.func.call(this.ctx, p,angle,d);
    };
    tcb.Callback = Callback;
})();

(function(){
    var Utils = {
        scale: 1,
        scaleReal:0,
        planeSpeed:1,
        STAGE_W:0,
        speedScale:0,
        STAGE_H:0,
        HALF_W:0,
        HALF_H:0,
        SLINGSHOT_W:0,
        SLINGSHOT_H:0,
        SLINGSHOT_X:0,
        SLINGSHOT_Y:0,
        BULLET_W:0,
        BULLET_H:0,
        BULLET_X:0,
        BULLET_Y:0,
        CLOTH_W:0,
        CLOTH_H:0,
        pathFactory:null,
        currentDistance:0,
        maxDistance:150,
        /**
         * 皮筋是否正在动画
         */
        isTween:false,
        isBullet:false,
        /**
         * 子弹发射
         */
        isBulletFire:false,
        /**
         * 延迟武器升级，如果在皮筋做动画的时候
         */
        isPreEnhance:false,
        pause:false,
        radianToDegreesFactor : 180 / Math.PI,
        degreeToRadiansFactor : Math.PI / 180,

        clearArray : function(A){
            while(A.length > 0) {
                A.pop();
            }
        },
        radToDeg:function  (radians) {
            return radians * this.radianToDegreesFactor;
        },
        degToRad : function(degrees) {
            return degrees * this.degreeToRadiansFactor;
        },
        /**
         * 计算两个点的角度值，PI/2 ~ -PI/2
         * @param point1
         * @param point2
         * @returns {number}
         */
        angleBetweenPoints : function (point1, point2) {
            return Math.atan2(point2.y - point1.y, point2.x - point1.x);
        },
        distance: function (p1,p2) {

            var dx = p1.x - p2.x;
            var dy = p1.y - p2.y;

            return Math.sqrt(dx * dx + dy * dy);

        },
        velocityFromRotation: function (rotation, speed, point) {

            if (speed === undefined) { speed = 60; }
            point = point || new tcb.Point();

            return point.set((Math.cos(rotation) * speed), (Math.sin(rotation) * speed));

        },

        /**
         * 计算飞机轨迹
         * @param v
         * @param k
         * @returns {*}
         */
        catmullRomInterpolation: function (v, k) {

            var m = v.length - 1;
            var f = m * k;
            var i = Math.floor(f);

            if (v[0] === v[m])
            {
                if (k < 0)
                {
                    i = Math.floor(f = m * (1 + k));
                }

                return this.catmullRom(v[(i - 1 + m) % m], v[i], v[(i + 1) % m], v[(i + 2) % m], f - i);
            }
            else
            {
                if (k < 0)
                {
                    return v[0] - (this.catmullRom(v[0], v[0], v[1], v[1], -f) - v[0]);
                }

                if (k > 1)
                {
                    return v[m] - (this.catmullRom(v[m], v[m], v[m - 1], v[m - 1], f - m) - v[m]);
                }

                return this.catmullRom(v[i ? i - 1 : 0], v[i], v[m < i + 1 ? m : i + 1], v[m < i + 2 ? m : i + 2], f - i);
            }

        },

        catmullRom: function (p0, p1, p2, p3, t) {

            var v0 = (p2 - p0) * 0.5, v1 = (p3 - p1) * 0.5, t2 = t * t, t3 = t * t2;

            return (2 * p1 - 2 * p2 + v0 + v1) * t3 + (-3 * p1 + 3 * p2 - 2 * v0 - v1) * t2 + v0 * t + p1;
        },
        /**
         *  重新计算canvas长宽
         * @param canvasID
         */
        resizeCanvas:function(canvasID){
            var clientWidth = Math.max(window.innerWidth, document.documentElement.clientWidth);
            var clientHeight = Math.max(window.innerHeight, document.documentElement.clientHeight);

            console.log('width ' + clientWidth);
            //if (clientWidth >= 1080){
            //    clientWidth = clientHeight * 0.6;
            //}

            var c = document.getElementById(canvasID);
            c.width = clientWidth;
            c.height = clientHeight;

            this.STAGE_W = clientWidth;
            this.STAGE_H = clientHeight;
            var sc = clientWidth / 1080;
            this.scale = sc > 1 ? 1 : sc;
            this.scaleReal = sc;
            //alert(clientWidth);
            this.maxDistance *= this.scale;

            return clientWidth > 980;
        },

        rectangleToRect:function(r){
            return new createjs.Graphics.Rect(r.x, r.y, r.width, r.height);
        }

    };

    Utils.isScore = false;

//Utils.isDebug = true;//本地测试使用,弃用
    Utils.debugB = false;//测试碰撞区
    Utils.debugF = false;//测试FPS
    Utils.getData = function(){
        if (DEBUG) {
            var data = {
                "bullet_count":30,//子弹数量
                "bullet_gold_count":5,
                "plane_empty":0,//空飞机
                "plane_gift":0,//礼品飞机
                "plane_gold":2,//金刚蛋飞机
                "plane_ticket":6,//劵
                "plane_cash":1,//现金飞机
                "fly_bird":true//是否有鸟
            };
            return data;
        }

        return getData();
    };
    Utils.noBullets = function(){
        console.log(' bullet is empty');
        if(!DEBUG && !Utils.isScore){
            //Utils.pause = true;
            noBullets();
        }
    };

    Utils.getScroe = function(name, stoneCount,goldCount){
        console.log('^_^ hit >> ' + name + ', stone count >> ' + stoneCount + ' gold count >> ' + goldCount);
        if (DEBUG) {
            return {"isbz": true, "score":20, "bullets":20, "goldbullets":5};
        }

        //Utils.pause = true;
        Utils.isScore = true;
        var result = getScroe(name, stoneCount,goldCount);
        console.log(result);
        return result;
    };

    Utils.hitEmpty = function(stoneCount,goldCount){
        console.log('hit empty plane - -' + ', stone count >> ' + stoneCount + ' gold count >> ' + goldCount);
        if(!DEBUG){
            hitEmpty(stoneCount,goldCount);
        }
    };

    Utils.slingshotDie = function(){
        Utils.pause = true;
        if (!DEBUG) {
            slingshotDie();
        }
    };

    Utils.showTidData = function(){
        console.log('war info click!');
        if (!DEBUG) {
            showTidData();
        }
    };
    tcb.Utils = Utils;
})();

(function(){
    var PlanePathFactory = function(width) {
        var Utils = tcb.Utils;
        this.baseY = 200 * Utils.scale;
        this.templatePoint = [];
        this.width = width;
        this.ty = 60 * Utils.scale;
        this.widthCount = 9;
        this.paths = [];
        var tx = this.width / (this.widthCount - 1);

        //this.templatePoint[-2] = new Array();
        //for (var i = -1; i < this.widthCount + 1; i ++){
        //    var node = {x:i * tx, y:this.baseY - this.ty * 2};
        //    this.templatePoint[-2][i] = node;
        //}

        this.templatePoint[-1] = new Array();
        for (var i = -4; i < this.widthCount + 4; i ++){
            var node = {x:i * tx, y:this.baseY - this.ty};
            this.templatePoint[-1][i] = node;
        }
        this.templatePoint[0] = new Array();
        for (var i = -4; i < this.widthCount+4; i ++){
            var node = {x:i * tx, y:this.baseY};
            this.templatePoint[0][i] = node;
        }
        this.templatePoint[1] = new Array();
        for (var i = -4; i < this.widthCount + 4; i ++){
            var node = {x:i * this.width / (this.widthCount - 1), y:this.baseY + this.ty};
            this.templatePoint[1][i] = node;
        }

        //this.templatePoint[2] = new Array();
        //for (var i = -1; i < this.widthCount + 1; i ++){
        //    var node = {x:i * tx, y:this.baseY + this.ty * 2};
        //    this.templatePoint[2][i] = node;
        //}

    };
    PlanePathFactory.prototype.constructor = PlanePathFactory;
    PlanePathFactory.prototype.createPath = function(indexs){
        var path = {
            x:[],
            y:[],
            paths:[]
        };

        for (var i = 0; i < indexs.v.length; i ++){
            var node = this.templatePoint[indexs.v[i]][indexs.h[i]];
            path.x[i] = node.x;
            path.y[i] = node.y;
        }

        return path;
    };

    PlanePathFactory.prototype.getLevelPath = function(index, level){
        var path = this.paths[index];
        var newpath = {
                x:path.x.slice(0),
                y:path.y.slice(0),
                paths:path.paths.slice(0)
            },
            incrementY = level * this.ty,
        //ix = 0,
        //x = 1 / this.width,
        //px,
        //py,
        //node,
            i;

        for (i = 0; i < newpath.y.length; i++){
            newpath.y[i] += incrementY;
        }

        //for (i = 0; i < 1; i += x){
        //    px = Utils.catmullRomInterpolation(newpath.x, i);
        //    py = Utils.catmullRomInterpolation(newpath.y, i);
        //    node = {x:px, y:py, angle:0};
        //    if (ix > 0) {
        //        node.angle = Utils.angleBetweenPoints(newpath.paths[ix - 1], node);
        //    }
        //
        //    newpath.paths.push(node);
        //    ix++;
        //}

        return newpath;
    };
    PlanePathFactory.prototype.initPath = function(){
        var indexs = [
                //1
                {
                    v:[ 0, -1, 1, -1, 1, -1, 0, 1, -1, 1, -1, 1,  0],
                    h:[-1,  0, 2,  4, 6,  8, 9, 8,  6, 4,  2, 0, -1]
                },
                //2
                {
                    v:[ 0, -1, 1, -1, 0, 1, -1, 1,  0],
                    h:[-1,  1, 4,  7, 9, 7,  4, 1, -1]
                },
                //3
                {
                    v:[ 0, -1, 1, -1, 0, 1, -1, 1,  0],
                    h:[-1,  0, 4,  8, 9, 8,  4, 0, -1]
                },
                //4
                {
                    v:[ 0, -1, 1, -1, 0, 1, -1, 1,  0],
                    h:[-1,  0, 5,  8, 9, 8,  3, 0, -1]
                },
                //5
                {
                    v:[ 0, -1, 1, -1, 0, 1, -1, 1,  0],
                    h:[-1,  0, 6,  8, 9, 8,  2, 0, -1]
                },
                //6
                {
                    v:[ 0, -1, 1, -1, 1, -1, 1,  0],
                    h:[-1,  0, 7,  8, 9,  2, 1, -1]
                },
                //7
                {
                    v: [ 0, -1, 1, 0, -1, 1,  0],
                    h: [-1,  0, 8, 9,  8, 0, -1]
                },
                //8
                {
                    v: [ 0, -1, 0, -1, 1, -1, 0, 1, 0, 1, -1, 1,  0],
                    h: [-1,  0, 1,  2, 5,  8, 9, 8, 7, 6,  3, 1, -1]
                },
                //9
                {
                    v: [ 0, -1, 1, -1, 1, 0, -1, 1, 0, 1,  0],
                    h: [-1,  1, 2,  4, 8, 9,  7, 5, 4, 3, -1]
                }
            ],
            i,
            N = indexs.length,
            index;

        for (i = 0; i < N; i++){
            index = indexs[i];
            this.paths.push(this.createPath(index));
        }

        var birdIndex = {
            v: [ 0, -1, 0, -1, 0, -1, 0, -1, 0, -1,  0],
            h: [-4, -2, 1,  5, 8, 12, 8,  5, 1, -2, -4]
        };
        this.paths[-1] = this.createPath(birdIndex);

        //clear templatePoint
        tcb.Utils.clearArray(this.templatePoint);
    };

    tcb.PlanePathFactory = PlanePathFactory;
})();
this.tcb = this.tcb || {};

(function () {
    /**
     * 多继承不行，用组合啊
     * @param hp
     * @constructor
     */
    var Life = function(hp, fixShow, offset){
        this.lifeBg = document.createElement('div');
        this.lifeBg.className = 'life_bg';

        this.lifeProBg = document.createElement('div');
        this.lifeProBg.className = 'life_border_bg';
        this.lifePro = document.createElement('div');
        this.lifePro.className = 'life_pro';

        this.lifeProBg.appendChild(this.lifePro);
        this.lifeBg.appendChild(this.lifeProBg);

        this.fixImg = document.createElement('img');
        this.fixImg.style.position = 'absolute';
        this.fixImg.style.opacity = 0;
        this.fixImg.src = 'images/flygame/fix_tip.png';

        this.hp = hp;
        this.maxHp = hp;
        this.w = 220;
        this.h = 38;
        this.proW = 190;
        this.offset = 10;
        this.lastState = 0;
        this.state = 0;//0正常,-1残血,-2虚血

        this.lassLoss = 0;

        this.target = null;
        this.isTween = false;

        this.isHide = false;

        this.fixShow = false;
        if (fixShow) {
            this.fixShow = fixShow;
        }
        if (offset){
            this.offset = offset;
        }

        this.lifeBg.onclick = function(){
            console.log('onClick');
        };
    };
    Life.prototype.setTarget = function(target){
        this.target = target;
    };
    Life.prototype.setOnclick = function(func){
        this.lifeBg.onclick = func;
    };
    Life.prototype.getRate = function(){
        return this.hp / this.maxHp;
    };

    Life.prototype.getLastLossRate = function(){
        return this.lassLoss / this.maxHp;
    };

    Life.prototype.isDie = function(){
        if (this.hp <= 0) {
            if (!this.isTween) {
                this.hide();
            }
            return true;
        }

        return false;
    };

    Life.prototype.show = function(){
        document.body.appendChild(this.lifeBg);

        if (this.fixShow) {
            document.body.appendChild(this.fixImg);
        }

        this.loss(0,true);
    };

    /**
     * 销毁
     */
    Life.prototype.destory = function(){
        document.body.removeChild(this.lifeBg);
    };

    Life.prototype.hide = function(){
        if (this.isHide) {
            return;
        }
        this.isHide = true;
        //document.body.removeChild(this.lifeBg);
        this.lifeBg.style.opacity = 0;

        if (this.target != null) {
            console.log('target invisible!!');
            this.target.visible = false;
        }

    };

    Life.prototype.reShow = function(x,y){
        this.lifeBg.style.opacity = 1;
        this.setPosition(x, y);
        this.hp = this.maxHp;
        this.loss(0);
        this.isHide = false;
    };

    /**
     * 设置显示位置
     * @param x
     * @param y
     */
    Life.prototype.setPosition = function(x,y){
        var targetX = x - this.w / 2;
        var targetY = y + this.offset;
        this.lifeBg.style.top = targetY + 'px';
        this.lifeBg.style.left = targetX + 'px';

        var imgX = targetX + this.w + 10;
        var imgY = targetY - 53;
        this.fixImg.style.top = imgY + 'px';
        this.fixImg.style.left = imgX + 'px';
    };

    /**
     * 复活
     */
    Life.prototype.revive = function(){
        this.hp = this.maxHp;
        this.loss(0);
    };

    /**
     * 掉当前的一半
     */
    Life.prototype.half = function(){
        this.hp /= 2;
        this.loss(0);
    };

    /**
     * 掉血
     * @param loss
     * return 状态
     */
    Life.prototype.loss = function(loss, show) {
        if (loss) {
            this.hp -= loss;
            this.lassLoss = loss;
        }

        if (!show) {
            this.showTween();
        }

        return this.changeState();
    };


    Life.prototype.changeState = function(){
        var rate = this.getRate();
        if (rate > 2/3 && rate <= 1) {
            this.state = 0;
            this.lifePro.style.backgroundColor = '#95c955';
        } else if (rate <= 2/3 && rate > 1/3) {
            this.state = -1;
            this.lifePro.style.backgroundColor = '#fee689';
        } else {
            this.state = -2;
            this.lifePro.style.backgroundColor = '#ff6068';
        }

        if (this.lastState != this.state) {
            //两种状态不一致，显示，tip
            console.log('show tip');
            //this.fixImg.style.opacity = 1;
            var tween = createjs.Tween.get({opacity:0})
                .to({opacity:1}, 1000, createjs.Ease.getPowIn(2));
            //.wait(2000);
            //.to({opacity:0}, 1000, createjs.Ease.getPowOut(2));
            tween.on('change',function(tween){
                var o = tween.target.target.opacity;
                this.fixImg.style.opacity = o;
            },this);
            tween.call(function(){
                //console.log('tip tween complete');
            });

            this.lastState = this.state;
        } else {
            this.fixImg.style.opacity = 0;
        }

        return this.state;
    };

    Life.prototype.showTween = function(){
        var rate = this.getRate();
        var width = this.proW * rate;
        var t = 1000 * this.getLastLossRate();
        var cw = this.lifePro.clientWidth;
        //console.log('client width = ' + cw);
        var tween = createjs.Tween.get({width: cw}).to({width: width}, t, createjs.Ease.getPowOut(2));
        this.isTween = true;
        tween.on('change',function(tween){
            var w = tween.target.target.width;
            //console.log('change ' + w);
            this.lifePro.style.width = w + 'px';
        },this);
        tween.call(function(){
            //console.log('loss tween complite!');
            this.isTween = false;
            this.isDie();
        },[], this);
    };

    tcb.Life = Life;
})();
this.tcb = this.tcb || {};
(function () {
    /**
     *资源加载器
     * @constructor
     */
    var ResourceLoader = function(){
        createjs.LoadQueue.call(this, true);
        this.ID_BG = 'bg';
        this.ID_SLINGSHOT = 'slingshot';
        this.ID_SLINGSHOT_PLUS = 'slingshot_plus';
        this.ID_BULLET = 'bullet';
        this.ID_BULLET_GOLD = 'bullet_gold';
        this.ID_BULLET_COUNT = 'bullet_count';
        this.ID_BULLET_GOLD_COUNT = 'bullet_gold_count';
        this.ID_PLANE_CASH = 'plane_cash';
        this.ID_PLANE_EMPTY = 'plane_empty';
        this.ID_PLANE_GIFT = 'plane_gift';
        //this.ID_PLANE_GOLD = 'plane_gold';
        this.ID_PLANE_TICKET = 'plane_ticket';
        this.ID_PLANE_BIRD = 'fly_bird';
        this.ID_CLOTH = 'cloth';
        this.ID_CLOUD = 'cloud';
        this.ID_POINTER = 'pointer';
        this.ID_CROW = 'crow';
        this.ID_SHIT = 'shit';
        this.ID_CRAZY_PLANE = 'crazy_plane';
        this.ID_CRAZY_BIRD = 'crazy_bird';
        this.ID_BANG = 'bang';
        this.ID_SCORE = 'score';

        this.manifest = [
            {src: 'bg_.png', id: this.ID_BG},

            //{src: 'slingshot.png', id: this.ID_SLINGSHOT},

            {src: 'bullet.png', id: this.ID_BULLET},
            {src: 'bullet_gold.png', id: this.ID_BULLET_GOLD},
            {src: 'bullet_count.png', id: this.ID_BULLET_COUNT},
            {src: 'bullet_gold_count.png', id: this.ID_BULLET_GOLD_COUNT},
            {src: 'cloth.png', id: this.ID_CLOTH},
            {src: 'cloud.png', id: this.ID_CLOUD},
            {src: 'pointer.png', id: this.ID_POINTER},
            {src: 'shit.png', id: this.ID_SHIT},
            {src: 'score_.png', id: this.ID_SCORE},

            {src: 'plane_cash.json', id: this.ID_PLANE_CASH, type: 'spritesheet'},
            {src: 'plane_empty.json', id: this.ID_PLANE_EMPTY, type: 'spritesheet'},
            {src: 'plane_gift.json', id: this.ID_PLANE_GIFT, type: 'spritesheet'},
            //{src: 'plane_gold.json', id: this.ID_PLANE_GOLD, type: 'spritesheet'},
            {src: 'plane_ticket.json', id: this.ID_PLANE_TICKET, type: 'spritesheet'},
            {src: 'birdsheet.json', id: this.ID_PLANE_BIRD, type: 'spritesheet'},

            {src: 'slingshot_sheet.json', id: this.ID_SLINGSHOT, type: 'spritesheet'},
            {src: 'slingshot_plus_sheet.json', id: this.ID_SLINGSHOT_PLUS, type: 'spritesheet'},
            {src: 'crow_sheet.json', id: this.ID_CROW, type: 'spritesheet'},
            {src: 'crazy_plane.json', id: this.ID_CRAZY_PLANE, type: 'spritesheet'},
            {src: 'crazy_bird.json', id: this.ID_CRAZY_BIRD, type: 'spritesheet'},
            {src: 'bang.json', id: this.ID_BANG, type: 'spritesheet'},

        ];
        this.basePath = 'images/flygame/';

    };

    ResourceLoader.prototype = Object.create(createjs.LoadQueue.prototype);
    ResourceLoader.prototype.constructor = ResourceLoader;
    /**
     * 开始加载资源
     */
    ResourceLoader.prototype.startLoad = function(){
        this.loadManifest(this.manifest,true,this.basePath);
    };

    tcb.ResourceLoader = ResourceLoader;
})();

(function () {
    /**
     * 加载资源进度条 dom.
     *
     * @constructor
     */
    var LoaderDOM = function(){
        this.text = document.createElement('div');
        this.text.className = 'pre_load';
        this.text.innerHTML = '正在进入游戏 ...';


        this.probg = document.createElement('div');
        this.probg.className = 'progress_bg';


        this.pro = document.createElement('div');
        this.pro.className = 'progress';

        this.start = 0;
        this.flag = false;
    };

    /**
     * 记录一个时间值
     */
    LoaderDOM.prototype.add = function(){
        this.start = Date.now();
    };

    /**
     * 真正的添加，ui到dom中。
     */
    LoaderDOM.prototype.realAdd = function(){
        document.body.appendChild(this.text);
        document.body.appendChild(this.probg);
        document.body.appendChild(this.pro);
        this.flag = true;
    };

    /**
     * 从dom中移除ui
     */
    LoaderDOM.prototype.destory = function(){
        if (this.flag){
            document.body.removeChild(this.text);
            document.body.removeChild(this.probg);
            document.body.removeChild(this.pro);
        }

    };

    /**
     * 进度条
     * @param width
     */
    LoaderDOM.prototype.progress = function(width){
        this.pro.style.width = width + 'px';

        if (!this.flag){
            var cur = Date.now();
            if (cur - this.start > 500){
                this.realAdd();
            }
        }

    };

    tcb.LoaderDOM = LoaderDOM;
})();

this.tcb = this.tcb || {};

(function () {
    var RubberContainer = function(){
        createjs.Container.call(this);
        this.rubber = new createjs.Shape();
        this.cloth = null;
        this.target = new tcb.Point();
        this.left = new tcb.Point();
        this.right = new tcb.Point();
        this.center = new tcb.Point();
        this.w = 0;
        this.h = 0;
        this.baseT = 35;
        this.tT = 75;

        this.tweenComplete = function(){};
        this.tweenCompleteContext = this;
    };
    RubberContainer.prototype = Object.create(createjs.Container.prototype);
    RubberContainer.prototype.constructor = RubberContainer;
    RubberContainer.prototype.initChild = function(){
        var xoffset = 34, yoffset=46, Utils = tcb.Utils;
        this.left.x = Utils.SLINGSHOT_X - Utils.SLINGSHOT_W/2 + xoffset * Utils.scale;
        this.left.y = Utils.SLINGSHOT_Y - Utils.SLINGSHOT_H / 2 + yoffset * Utils.scale;
        this.right.x = Utils.SLINGSHOT_X + Utils.SLINGSHOT_W / 2 - xoffset * Utils.scale;
        this.right.y = this.left.y;

        var loader = this.stage.loader;
        var bmp = loader.getResult(loader.ID_CLOTH);
        this.w =  bmp.width;
        this.h =  bmp.height;
        Utils.CLOTH_W = bmp.width * Utils.scale;
        Utils.CLOTH_H = bmp.height * Utils.scale;

        this.cloth = new createjs.Bitmap(bmp);

        this.target.x = Utils.BULLET_X;
        this.target.y = Utils.BULLET_Y;
        this.addChild(this.cloth);

        this.center.set(Utils.HALF_W, this.left.y);

        this.addChild(this.rubber);
        this.drawRubber();
    };

    RubberContainer.prototype.loop = function(evt){
    };

    RubberContainer.prototype.drawRubber = function(target,angle,d){
        var g = this.rubber.graphics,Utils = tcb.Utils;
        g.clear();
        g.setStrokeStyle(10 * Utils.scale, 'round', 'round');
        g.beginStroke("#F0F");
        g.beginFill("#F0F");
        //g.rect(this.center.x,this.center.y,1,1);

        //g.rect(Utils.SLINGSHOT_X - Utils.SLINGSHOT_W / 2, Utils.SLINGSHOT_Y - Utils.SLINGSHOT_H / 2, 260, 240);

        if (!angle){
            angle = Math.PI / 2;
        }

        if (!d){
            d = 0;
        }

        var ro = angle - Math.PI / 2;
        if (target){
            this.target = target;
        }

        var h = Utils.BULLET_H/2 + Utils.CLOTH_H/2 - 5 * Utils.scale;
        this.target.x = this.target.x + h * Math.cos(angle);
        this.target.y = this.target.y + h * Math.sin(angle);

        //console.log(Utils.radToDeg(ro));
        //g.rect(0, Utils.STAGE_H * 2 / 3,Utils.STAGE_W,1);

        g.beginStroke("#93690A");
        g.beginFill("#93690A");

        h = this.w /2 - 8;
        h = h * Utils.scale;

        g.moveTo(this.left.x, this.left.y);
        g.lineTo(this.target.x - h * Math.cos(ro), this.target.y - h * Math.sin(ro));

        g.moveTo(this.right.x,this.right.y);
        g.lineTo(this.target.x + h * Math.cos(ro), this.target.y + h * Math.sin(ro));

        this.cloth.setTransform(this.target.x, this.target.y,Utils.scale,Utils.scale);
        this.cloth.rotation = Utils.radToDeg(ro);
        this.cloth.regX = this.w / 2;
        this.cloth.regY = this.h / 2;
    };

    RubberContainer.prototype.up = function(evt){
        var Utils = tcb.Utils;
        var tx = this.target.x - this.center.x,
            ty = this.target.y - this.center.y,
            lt = new tcb.Point(this.center.x - tx, this.center.y - ty),
            rc = new tcb.Point(this.center.x + tx / 2, this.center.y + ty / 2),
            lc = new tcb.Point(this.center.x - tx / 2, this.center.y - ty / 2),
            c = new tcb.Point(Utils.BULLET_X, Utils.BULLET_Y),
            t = this.baseT + this.tT * Utils.currentDistance / Utils.maxDistance;
        //t = 50 + 150 * Utils.currentDistance / Utils.maxDistance;

        var tween = createjs.Tween.get(this.target).to({x:lt.x,y:lt.y},t * 4)
            .to({x:rc.x,y:rc.y}, t * 3)
            .to({x:lc.x,y:lc.y}, t * 2)
            .to({x: c.x,y: c.y}, t * 2.5);
        Utils.isTween = true;
        tween.on("change", function(){
            this.drawRubber();
        },this);

        tween.call(this.tweenComplete,[],this.tweenCompleteContext);
    };

    RubberContainer.prototype.enhance = function(){
        console.log('皮筋 升级');
        var xoffset = -14, yoffset=76, Utils = tcb.Utils;
        this.left.x = Utils.SLINGSHOT_X - Utils.SLINGSHOT_W/2 + xoffset * Utils.scale;
        this.left.y = Utils.SLINGSHOT_Y - Utils.SLINGSHOT_H / 2 + yoffset * Utils.scale;
        this.right.x = Utils.SLINGSHOT_X + Utils.SLINGSHOT_W / 2 - xoffset * Utils.scale;
        this.right.y = this.left.y;
        this.baseT = 25;
        this.tT = 45;

        if (!tcb.Utils.isTween) {
            this.drawRubber();
        }
    };

    tcb.RubberContainer = RubberContainer;
})();
this.tcb = this.tcb || {};

(function () {
    /**
     * 弹弓
     * @param loader
     * @constructor
     */
    var SlingShot = function(key,loader, hp){
        var s = loader.getResult(key);
        createjs.Sprite.call(this, s, 'normal');

        //this.scaleX = Utils.scale;
        //this.scaleY = Utils.scale;
        var b = this.getTransformedBounds();
        //console.log(b.toString());
        this.w = b.width;
        this.h = b.height;
        this.regX = b.width/2;
        this.regY = b.height/2;
        this.x = tcb.Utils.HALF_W;
        this.y = tcb.Utils.STAGE_H - 300 * tcb.Utils.scale;

        this.undying = false;

        var _hp = 100;
        this.life = new tcb.Life(_hp, false, -10);

        this.life.setPosition(this.x, this.y + this.h / 2);
        this.life.show();
        var that = this;
        this.life.setOnclick(function(){
            that.test();
        });

        if (hp){
            console.log('_hp ' + _hp + ', hp ' + hp);
            //继承之前弹弓的血量
            this.life.loss(_hp - hp);
        }

    };

    SlingShot.prototype = Object.create(createjs.Sprite.prototype);
    SlingShot.prototype.constructor = SlingShot;

    SlingShot.prototype.getB = function(){
        var b, r, x, y;
        b = this.getTransformedBounds();
        x = b.x;
        y = b.y + b.height / 2;
        r = new createjs.Rectangle(x, y, this.w, this.h/2);
        return r;
    };

    SlingShot.prototype.hit = function(bullet){
        bullet.visible = false;

        this.loss(bullet.power);
        console.log(bullet.name + ' hit slingshot, hp > ' + this.life.hp);
    };

    SlingShot.prototype.loss = function(loss){
        var state = this.life.loss(loss);
        if (state == 0) {
            this.gotoAndPlay('normal');
        } else if(state == -1) {
            this.gotoAndPlay('yellow');
        } else if(state == -2) {
            this.gotoAndPlay('red');
        }

        if (this.life.isDie()) {
            console.log('slingshot is die!!!!');
            tcb.Utils.slingshotDie();
        }
    };
    SlingShot.prototype.test = function(){
        console.log('slingshot life hp >> ' + this.life.hp);
    };

    SlingShot.prototype.destory = function(){
        this.life.destory();
    };

    tcb.SlingShot = SlingShot;

})();

(function () {
    var Score = function(bmp){
        createjs.Container.call(this);

        var score = new createjs.Bitmap(bmp);
        score.regX = bmp.width/2;
        score.regY = bmp.height/2;
        score.visible = false;
        this.addChild(score);

        var scoreHint = new createjs.Text('0','26pt verdana','#000');
        scoreHint.textAlign = 'right';
        scoreHint.y = score.y;
        scoreHint.regY = bmp.height/ 6;
        scoreHint.visible = false;
        this.addChild(scoreHint);
        this.scoreW = bmp.width;
        this.score = score;
        this.hint = scoreHint;
    };

    Score.prototype = Object.create(createjs.Container.prototype);
    Score.prototype.constructor = Score;
    Score.prototype.show = function(x, y){
        this.score.x = x;
        this.score.y = this.hint.y = y;
        this.hint.x = this.scoreW / 6 + x;
        this.score.visible = true;
        this.hint.visible = true;
    };

    Score.prototype.setText = function (text){
        if (text) {
            this.hint.text = text;
        }
    };

    Score.prototype.setOnclickBounds = function(func){
        var bounds = this.getBounds();
        this.b = document.createElement('div');
        this.b.className = 'click_bounds';
        this.b.style.left = bounds.x + 'px';
        this.b.style.top = bounds.y + 'px';
        this.b.style.width = bounds.width + 'px';
        this.b.style.height = bounds.height + 'px';
        this.b.onclick = func;
        document.body.appendChild(this.b);
    };

    tcb.Score = Score;

})();

(function () {
    /**
     * 静态容器，放弹弓，积分
     * @constructor
     */
    var StaticContainer = function(){
        createjs.Container.call(this);
        this.slingshot = null;
        this.bang = null;
        this.score = null;
        this.isEnhance = false;
    };
    StaticContainer.prototype = Object.create(createjs.Container.prototype);
    StaticContainer.prototype.constructor = StaticContainer;

    StaticContainer.prototype.initChild = function(){
        var loader = this.stage.loader;
        var bmp = loader.getResult(loader.ID_BG),
            scaleX = tcb.Utils.STAGE_W / bmp.width,
            scaleY = tcb.Utils.STAGE_H / bmp.height;

        var bg = new createjs.Bitmap(bmp);
        bg.setTransform(0,0,scaleX,scaleY);
        this.addChild(bg);

        tcb.Utils.SLINGSHOT_X = tcb.Utils.HALF_W;
        tcb.Utils.SLINGSHOT_Y = tcb.Utils.STAGE_H - 300 * tcb.Utils.scale;

        var slingshot = new tcb.SlingShot(loader.ID_SLINGSHOT,loader);
        //console.log('slingshot width = ' + slingshot.w);
        //console.log('scale = ' + Utils.scale);
        tcb.Utils.SLINGSHOT_W = slingshot.w * tcb.Utils.scale;
        tcb.Utils.SLINGSHOT_H = slingshot.h * tcb.Utils.scale;
        this.addChild(slingshot);
        this.slingshot = slingshot;
        console.log('slingshot x='+slingshot.x +',y='+slingshot.y);

        //var that = this;
        //slingshot.life.setOnclick(function(){
        //    that.enhance();
        //});

        var bangsheet = loader.getResult(loader.ID_BANG);
        var bang = new createjs.Sprite(bangsheet, 'bang');
        bang.visible = false;
        bang.regX = bangsheet.getFrameBounds(0).width / 2;
        bang.regY = bangsheet.getFrameBounds(0).height;
        bang.x = tcb.Utils.SLINGSHOT_X;
        bang.y = tcb.Utils.SLINGSHOT_Y;
        bang.alpha = 0;
        this.addChild(bang);
        this.bang = bang;

        bmp = loader.getResult(loader.ID_SCORE);
        var score = new tcb.Score(bmp);
        var scoreX = tcb.Utils.STAGE_W - 180 * tcb.Utils.scale;
        score.show(scoreX, tcb.Utils.STAGE_H - 80 * tcb.Utils.scale);
        this.addChild(score);
        this.score = score;
        score.setOnclickBounds(tcb.Utils.showTidData);

        //var war = new WarInfo();
        //war.show(scoreX, Utils.STAGE_H - 140 * Utils.scale);
    };

    StaticContainer.prototype.overlap = function(bullet){
        if (bullet.overlap(this.slingshot)) {
            this.slingshot.hit(bullet);
            if(bullet.name == this.stage.loader.ID_CRAZY_PLANE ||  bullet.name == this.stage.loader.ID_CRAZY_BIRD) {
                this.showBangTween(bullet.name);
            }
        }
    };

    StaticContainer.prototype.showBangTween = function(name){
        var t = 400;
        if (name == this.stage.loader.ID_CRAZY_BIRD) {
            t = 800;
        }

        this.bang.visible = true;
        createjs.Tween.get(this.bang)
            .to({alpha:1}, t, createjs.Ease.getPowIn(2))
            .wait(t)
            .to({alpha:0}, t, createjs.Ease.getPowOut(2))
            .call(function(){});
    };

    /**
     * 设置积分
     * @param score
     */
    StaticContainer.prototype.setScore = function(score){
        //积分≥10升级武器
        if (score>=5) {
            this.enhance();
        }
        this.score.setText(score);
    };

    /**
     * 弹弓默认升级成铁弹弓，铁弹弓的攻击速度X5，攻击力X2
     */
    StaticContainer.prototype.enhance = function(){
        if (this.isEnhance) {
            return;
        }

        if (tcb.Utils.isTween) {
            tcb.Utils.isPreEnhance = true;
            return;
        }

        if (tcb.Utils.isPreEnhance) {
            tcb.Utils.isPreEnhance = false;
            console.log('延迟增强');
        }

        this.isEnhance = true;
        console.log('武器升级');
        var loader = this.stage.loader;
        var slingshot = new tcb.SlingShot(loader.ID_SLINGSHOT_PLUS, loader, this.slingshot.life.hp);
        this.removeChild(this.slingshot);
        this.slingshot.destory();

        this.addChildAt(slingshot, 1);
        this.slingshot = slingshot;
        this.stage.bullets.enhance();
        this.stage.rubber.enhance();
    };

    StaticContainer.prototype.slingshotFix = function(){
        this.slingshot.life.revive();
    };

    StaticContainer.prototype.slingshotHalf = function(){
        this.slingshot.life.half();
    };

    tcb.StaticContainer = StaticContainer;

})();
this.tcb = this.tcb || {};

(function () {
    var PaperPlane = function(key,loader,speed,level){
        var s = loader.getResult(key);
        createjs.Sprite.call(this,s);
        this.x = 0;
        this.y = 0;
        this.scaleX = tcb.Utils.scale;
        this.scaleY = tcb.Utils.scale;
        var b = this.getTransformedBounds();
        this.regX = b.width/2;
        this.regY = b.height/2;
        this.setBounds(0 + b.width / 3, 0 + b.height / 3, b.width / 3, b.height / 3);
        this.visible = false;
        this.path = null;
        this.pi = 0;
        this.w = b.width;
        this.h = b.height;
        this.name = key;
        this.lastPoint = null;
        this.speed = speed || 1;
        this.pathCount = tcb.Utils.STAGE_W / this.speed;
        //console.log('pathCount = ' + this.pathCount);
        this.level = level;
    };

    PaperPlane.prototype = Object.create(createjs.Sprite.prototype);
    PaperPlane.prototype.constructor = PaperPlane;
    PaperPlane.prototype.loop = function(evt){
        if (this.path == null) return;

        var px = tcb.Utils.catmullRomInterpolation(this.path.x, this.pi/this.pathCount);
        var py = tcb.Utils.catmullRomInterpolation(this.path.y, this.pi/this.pathCount);
        var point = new tcb.Point(px,py);
        var angle = this.lastPoint === null ? 0:tcb.Utils.angleBetweenPoints(this.lastPoint,point);
        this.lastPoint = point;
        this.x = px;
        this.y = py;

        if (this.pi > this.pathCount / 2){
            this.rotation = tcb.Utils.radToDeg(angle - Math.PI);
        } else {
            this.rotation = tcb.Utils.radToDeg(angle);
        }
        this.pi++;
        if (this.pi >= this.pathCount){
            this.pi = 0;
        }

        if (px > tcb.Utils.STAGE_W + this.w){
            this.gotoAndPlay('left');
        } else if (px < 0 - this.w){
            this.gotoAndPlay('right');
        }

    };
    PaperPlane.prototype.show = function(path, reshow){
        this.visible = true;
        if (reshow){
            this.pi = 0;
            tcb.Utils.clearArray(this.path.paths);
            tcb.Utils.clearArray(this.path.x);
            tcb.Utils.clearArray(this.path.y);
        } else {
            this.pi = Math.floor(Math.random() * this.pathCount * 2 / 4) + this.pathCount/4;
        }

        this.path = path;

        if (this.pi < this.pathCount/2) {
            this.gotoAndPlay('right');
        } else if(this.pi > this.pathCount/2){
            this.gotoAndPlay('left');
        }
    };
    PaperPlane.prototype.drawPath = function(g){
        for (var i = 0; i < this.path.paths.length; i++) {
            g.rect(this.path.paths[i].x, this.path.paths[i].y, 1, 1);
        }

        for (var i = 0; i < this.path.x.length; i++) {
            g.rect(this.path.x[i] - 3, this.path.y[i] - 3, 6,6);
        }
    };
    PaperPlane.prototype.getB = function(){
        var b = this.getTransformedBounds(), r, x, y;
        if (this.name == 'fly_bird' && false){//false 关闭小鸟 难度加大功能
            x = b.x + b.width/3 - this.w / 6;
            y = b.y + b.height/3 - this.h / 6;
            r = new createjs.Rectangle(x, y, this.w/3, this.h/3);
        } else {
            x = b.x + b.width/2 - this.w / 4;
            y = b.y + b.height/2 - this.h / 4;
            r = new createjs.Rectangle(x, y, this.w/2, this.h/2);
        }
        return r;
    };

    /**
     * 被子弹击中
     * @param bullet
     */
    PaperPlane.prototype.hit = function(bullet){
        console.log('overlap plane >> ' + this.name);
        this.visible = false;
        bullet.visible = false;
        tcb.Utils.isBullet = false;
        if (!tcb.Utils.isTween){
            this.stage.bullets.reShow();
        }
        this.stage.planes.showOne(this, true);

        var crazy = false;
        if (this.name == this.stage.loader.ID_PLANE_EMPTY){
            tcb.Utils.hitEmpty(this.stage.bullets.getStoneCount(), this.stage.bullets.getGoldCount());
        } else {
            var result = tcb.Utils.getScroe(this.name, this.stage.bullets.getStoneCount(), this.stage.bullets.getGoldCount());
            console.log(result);
            //TODO 设置积分
            if (!result.isbz) {
                this.stage.staticContainer.setScore(result.score);

                //if (this.name == this.stage.loader.ID_PLANE_BIRD) {
                    this.stage.bullets.setCount(result.bullets, result.goldbullets);
                    if ('full' == result.catapult) {
                        this.stage.staticContainer.slingshotFix();
                    } else if('halve' == result.catapult){
                        this.stage.staticContainer.slingshotHalf();
                    }
                //}

            }

            crazy = result.isbz;
        }

        return crazy;
    };

    tcb.PaperPlane = PaperPlane;
})();

(function () {
    /**
     * 乌鸦
     * @param key
     * @param loader
     * @param shit
     * @constructor
     */
    var Crow = function(key,loader,shit){
        tcb.PaperPlane.call(this,key,loader);
        this.visible = true;
        this.x = tcb.Utils.HALF_W;
        this.y = -150;
        this.life = new tcb.Life(100);

        this.life.setPosition(this.x, this.y + this.h / 2);
        this.life.show();
        this.life.setTarget(this);
        //this.life.loss(4);

        this.gotoAndPlay('fly');
        this.speed = 2;
        this.realSpeed = this.speed;

        this.shit = shit;
        this.initShit();
    };

    Crow.prototype = Object.create(tcb.PaperPlane.prototype);
    Crow.prototype.constructor = Crow;

    Crow.prototype.initShit = function(){
        if (this.shit != null) {
            this.shit.x = tcb.Utils.HALF_W;
            this.shit.speed.set(0, 800);
            this.shit.visible = false;
        }

    };
    Crow.prototype.loop = function(evt){
        this.y += this.realSpeed;
        this.life.setPosition(this.x, this.y + this.h / 2);

        if (this.y >= 150) {
            this.realSpeed = -this.speed;
            this.shit.visible = true;
            if (this.shit != null && this.shit.visible) {
                this.shit.y = this.y;
            } else {
                console.log('乌鸦没石头了');
            }

        } else if (this.y <= -150) {
            this.realSpeed = this.speed;
        }


    };
    Crow.prototype.show = function(){

    };
    Crow.prototype.hit = function(bullet){
        bullet.visible = false;
        tcb.Utils.isBullet = false;
        if (!tcb.Utils.isTween){
            this.stage.bullets.reShow();
        }
        this.life.loss(bullet.power);
        if (this.life.isDie()) {
            //this.visible = false;
            var result = tcb.Utils.getScroe('crow', this.stage.bullets.getStoneCount(), this.stage.bullets.getGoldCount());
            //TODO 设置积分
            if (!result.isbz) {
                this.stage.staticContainer.setScore(result.score);
            }
            var that = this;
            setTimeout(function(){
                console.log('crow boss reshow !!');
                that.reShow();
            }, 60 * 1000);
        }

        console.log('hit crow , hp > ' + this.life.hp);
    };
    Crow.prototype.reShow = function(){
        this.visible = true;
        this.y = -150;
        this.life.reShow(this.x, this.y);
    };

    tcb.Crow = Crow;
})();


(function () {
    var CrazyPlane = function(key,loader){
        tcb.PaperPlane.call(this,key,loader);
        this.ta = 1;
        this.tx = 1;
        this.ty = 1;
        this.p1 = null;
        this.p2 = null;
        if (key == loader.ID_CRAZY_BIRD) {
            this.power = 30;
        } else {
            this.power = 5;
        }
        this.angle = 0;
    };

    CrazyPlane.prototype = Object.create(tcb.PaperPlane.prototype);
    CrazyPlane.prototype.constructor = CrazyPlane;

    /**
     * 飞机
     * @param p1 飞机的位置，
     * @param p2 飞机要飞走的位置
     */
    CrazyPlane.prototype.setA = function(p1,p2) {
        console.log(this.name + ' is crazy !');
        var angle = tcb.Utils.angleBetweenPoints(p1, p2);
        this.angle = this.rotation = tcb.Utils.radToDeg(angle);

        var tx = 0;
        if (this.angle < 90) {
            tx = p2.x - p1.x;
            this.gotoAndPlay('right');
        } else if (this.angle > 90) {
            tx = p1.x - p2.x;
            this.gotoAndPlay('left');
            this.rotation -= 180;
        } else {
            this.gotoAndPlay('left');
            this.rotation -= 180;
        }
        var ty = p2.y - p1.y;
        var ta =  tx / ty;
        this.ta = ta;
        this.tx = tx;
        this.ty = ty;

        this.x = p1.x;
        this.y = p1.y;

        this.visible = true;

        this.p2 = p2;
        this.p1 = p1;

        //console.log('x ' + this.x + ', y ' + this.y + ', angle' + this.rotation + ', ta' + ta);
    };
    CrazyPlane.prototype.loop = function(evt){
        if (this.y >= this.p2.y) {
            this.visible = false;
            return;
        }
        this.y += 10;
        if (this.angle < 90) {
            this.x = this.p1.x + this.ta * (this.y - this.p1.y);
        } else if (this.angle > 90) {
            this.x = this.p1.x - this.ta * (this.y - this.p1.y);
        }
        //console.log('x ' + this.x);
    };

    CrazyPlane.prototype.overlap = function(target){
        var b = false;
        if (this.visible && target.visible && this.getB().intersects(target.getB())) {
            target.hit(this);
            b = true;
        }
        return b;
    };

    tcb.CrazyPlane = CrazyPlane;
})();

(function () {
    var Planes = function(){
        createjs.Container.call(this);
        tcb.Utils.pathFactory = new tcb.PlanePathFactory(tcb.Utils.STAGE_W);
        this.crazy = null;
        this.crazyBird = null;
        this.crazyPlane = null;
        this.crow = null;
        this.shit = null;
    };

    Planes.prototype = Object.create(createjs.Container.prototype);
    Planes.prototype.constructor = Planes;
    Planes.prototype.initChild = function(data){
        var loader = this.stage.loader;
        var crow = new tcb.Crow(loader.ID_CROW, loader, this.shit);
        this.addChild(crow);
        this.crow = crow;

        var crazyPlane = new tcb.CrazyPlane(loader.ID_CRAZY_PLANE, loader);
        var crazyBird = new tcb.CrazyPlane(loader.ID_CRAZY_BIRD, loader);
        this.addChild(crazyPlane);
        this.addChild(crazyBird);
        this.crazyBird = crazyBird;
        this.crazyPlane = crazyPlane;

        //this.crazy = crazyPlane;

        var plane = null;
        if (data.fly_bird){
            plane = new tcb.PaperPlane(loader.ID_PLANE_BIRD, loader,1,0);
            this.addChild(plane);
        }

        if (data.plane_cash > 0) {
            for (var i = 0; i < data.plane_cash; i++){
                plane = new tcb.PaperPlane(loader.ID_PLANE_CASH, loader, 5, 0);
                this.addChild(plane);
            }
        }

        //金刚蛋 飞机
        //if (data.plane_gold > 0) {
        //    for (var i = 0; i < data.plane_gold; i++){
        //        plane = new tcb.PaperPlane(loader.ID_PLANE_GOLD, loader,2.5, 1);
        //        this.addChild(plane);
        //    }
        //}

        //去掉礼品飞机
        if (data.plane_gift > 0) {
            for (var i = 0; i < data.plane_gift; i++){
                plane = new tcb.PaperPlane(loader.ID_PLANE_GIFT, loader,1.5, 1);
                this.addChild(plane);
            }
        }

        if (data.plane_empty > 0){
            plane = new tcb.PaperPlane(loader.ID_PLANE_EMPTY, loader,1, 1);
            this.addChild(plane);
            data.plane_empty--;
        }

        if (data.plane_ticket > 0) {
            for (var i = 0; i < data.plane_ticket; i++){
                plane = new tcb.PaperPlane(loader.ID_PLANE_TICKET, loader, 2, 2);
                this.addChild(plane);
            }
        }

        if (data.plane_empty > 0){
            plane = new tcb.PaperPlane(loader.ID_PLANE_EMPTY, loader,0.8, 1);
            this.addChild(plane);
            data.plane_empty--;
        }


        if (data.plane_empty > 0) {
            for (var i = 0; i < data.plane_empty; i++){
                if (i % 2 == 0){
                    plane = new tcb.PaperPlane(loader.ID_PLANE_EMPTY, loader,1,4);
                } else {
                    plane = new tcb.PaperPlane(loader.ID_PLANE_EMPTY, loader,0.8,5);
                }
                this.addChild(plane);
            }
        }

        tcb.Utils.pathFactory.initPath();
        this.show();
    };

    Planes.prototype.loop = function(evt){
        var N = this.children.length,
            i,p;

        for (i = 0; i < N; i ++){
            p = this.getChildAt(i);
            if (p.visible) {
                p.loop(evt);
            }
        }

    };
    Planes.prototype.show = function(){
        var i,
            plane,
            N = this.children.length;
        for (i=0; i<N; i++){
            plane = this.getChildAt(i);
            this.showOne(plane);
        }
    };

    Planes.prototype.showOne = function(plane,reShow){
        //乌鸦
        if (plane.name == this.stage.loader.ID_CROW || plane.name == this.stage.loader.ID_CRAZY_PLANE || plane.name == this.stage.loader.ID_CRAZY_BIRD) {
            return;
        }

        var reshow = reShow || false;
        var path,
            PN = tcb.Utils.pathFactory.paths.length,
            index,
            loader = this.stage.loader;
        index = Math.floor(Math.random() * PN);

        if (plane.name == loader.ID_PLANE_BIRD){
            index = -1;
        }
        path = tcb.Utils.pathFactory.getLevelPath(index, plane.level);
        plane.show(path,reshow);
    };

    Planes.prototype.drawPath = function(g){
        var N = this.children.length;
        for(var i = 0; i < N; i++){
            this.getChildAt(i).drawPath(g);
        }
    };

    Planes.prototype.overlap = function(bullet){
        var planes = this.children,
            N = planes.length,
            p,
            i;
        if (planes.length > 0){
            for (i = 0; i < N; i++){
                p = planes[i];

                if (p.name == this.stage.loader.ID_CRAZY_BIRD || p.name == this.stage.loader.ID_CRAZY_PLANE) {
                    //暴走飞机不被子弹打中
                    continue;
                }

                var point = new tcb.Point(p.x, p.y);
                if (bullet.overlap(p)) {
                    //console.log(point.x + ' , y ' + point.y);

                    //暴走
                    if (p.hit(bullet)) {
                        if (p.name == this.stage.loader.ID_PLANE_BIRD) {
                            this.crazyBird.setA(point, new tcb.Point(tcb.Utils.SLINGSHOT_X, tcb.Utils.SLINGSHOT_Y));
                            this.crazy = this.crazyBird;
                        } else if (p.name == this.stage.loader.ID_CROW){
                            //乌鸦，不暴走。
                        } else {
                            this.crazyPlane.setA(point, new tcb.Point(tcb.Utils.SLINGSHOT_X, tcb.Utils.SLINGSHOT_Y));
                            this.crazy = this.crazyPlane;
                        }
                    }

                    return ;
                }
            }
        }
    };

    Planes.prototype.getCrazy = function(){
        return this.crazy;
    };

    Planes.prototype.isCrazy = function(){
        if (this.crazy != null && this.crazy.visible) {
            return true;
        }

        return false;
    };
    Planes.prototype.initCrow = function(shit){
        this.shit = shit;
    };

    tcb.Planes = Planes;
})();
this.tcb = this.tcb || {};
(function () {
    /**
     * 子弹
     * @param bmp
     * @param type
     * @param count
     * @constructor
     */
    var Bullet = function(bmp, type, count){
        createjs.Bitmap.call(this, bmp);

        this.speed = new tcb.Point(0,0);
        this.gravity = new tcb.Point(0,0);
        this.w = bmp.width;
        this.h = bmp.height;
        this.regX = this.w / 2;
        this.regY = this.h / 2;
        //手指按下的地方
        this.positionDown = new tcb.Point();
        //手指移动时时位置
        this.position = new tcb.Point();

        this.cAngle = 0;
        this.cDistance = 0;
        this.maxDistance = tcb.Utils.maxDistance;
        this.baseSpeed = 1000;
        this.tspeed = 1000;
        this.baseGravityY = 800;
        this.type = type;
        this.count = count;
        if (this.type == 'stone') {
            this.power = 1;
        } else if(this.type == 'gold') {
            this.power = 10;
            this.baseSpeed = 1200;
        } else if(this.type == 'shit') {
            this.power = 3;
        }
        this.name = this.type;

        this.moveCallback = null;
        this.emptyCallback = function(){

        };
        this.setBounds(this.w / 10, this.h / 10,this.w * 4/5, this.h * 4 / 5);

        this.isDown = false;
    };

    Bullet.prototype = Object.create(createjs.Bitmap.prototype);
    Bullet.prototype.constructor = Bullet;

    Bullet.prototype.loop = function(evt){
        //if (!Utils.isBulletFire){
        //    return;
        //}

        //时间t增量
        var deltaS = evt.delta / 1000;
        //速度变量
        var s = this.speed.y * deltaS + this.gravity.y * deltaS * deltaS / 2;
        s = s * tcb.Utils.speedScale;
        this.speed.x = this.speed.x + this.gravity.x * deltaS;
        this.speed.y = this.speed.y + this.gravity.y * deltaS;
        this.x = this.x + this.speed.x * deltaS;
        this.y = this.y + s;
    };

    Bullet.prototype.down = function(evt){
        if (this.count <= 0) {
            return;
        }
        this.isDown = true;
        this.positionDown.set(evt.stageX,evt.stageY);
        this.position.set(evt.stageX,evt.stageY);
    };
    Bullet.prototype.up = function(evt){
        if (!this.isDown) {
            return false;
        }

        tcb.Utils.currentDistance = this.cDistance;
        this.gravity.y = this.baseGravityY;
        var s = this.baseSpeed + this.cDistance * this.tspeed / this.maxDistance;
        //s = s * tcb.Utils.speedScale;
        tcb.Utils.velocityFromRotation(this.cAngle, -s, this.speed);
        tcb.Utils.isBulletFire = true;
        this.isDown = false;
        console.log('speed y' + this.speed.y);
        this.use();
        return true;
    };

    Bullet.prototype.move = function(evt){
        if (!this.isDown){
            return;
        }

        this.position.set(evt.stageX,evt.stageY);
        this.cAngle = tcb.Utils.angleBetweenPoints(this.positionDown,this.position);

        if (this.cAngle < Math.PI / 6 || this.cAngle > Math.PI * 5 / 6) {
            return;
        }

        this.cDistance = tcb.Utils.distance(this.positionDown,this.position);
        this.cDistance = this.cDistance * 7 / 18;
        this.cDistance = Math.min(this.cDistance, this.maxDistance);

        this.x = tcb.Utils.BULLET_X + Math.cos(this.cAngle) * this.cDistance;
        this.y = tcb.Utils.BULLET_Y + Math.sin(this.cAngle) * this.cDistance;

        if (this.moveCallback != null){
            this.moveCallback.c(new tcb.Point(this.x,this.y),this.cAngle, this.cDistance);
        }

        this.rotation = tcb.Utils.radToDeg(this.cAngle - Math.PI/2);
    };


    Bullet.prototype.reset = function(){
        if (this.count <= 0) {
            //Utils.noBullets(this.name);
            this.emptyCallback();
            return;
        }

        this.speed = new tcb.Point(0,0);
        this.gravity = new tcb.Point(0,0);
        this.visible = true;
        this.setTransform(tcb.Utils.BULLET_X, tcb.Utils.BULLET_Y,tcb.Utils.scale,tcb.Utils.scale);
        this.regX = this.w / 2;
        this.regY = this.h / 2;
        this.cDistance = 0;
        this.cAngle = 0;
        tcb.Utils.isBullet = true;
        //Utils.isBulletFire = false;
    };

    Bullet.prototype.getB = function(){
        return this.getTransformedBounds();
    };

    Bullet.prototype.use = function(){
        this.count--;
    };

    /**
     * 检查是否和目标对象碰撞
     *
     * @param target
     */
    Bullet.prototype.overlap = function(target){
        var b = false;
        if (this.visible && target.visible && this.getB().intersects(target.getB())) {
            tcb.Utils.isBulletFire = false;
            //target.hit(this);
            b = true;
        }
        return b;
    };

    /**
     * 检查是否出界
     */
    Bullet.prototype.checkOutBounds = function(){
        var bound = this.getB();

        if (bound.x + bound.width < 0 || bound.y + bound.height < 0 || bound.x > tcb.Utils.STAGE_W || bound.y > tcb.Utils.STAGE_H){
            console.log(this.type + 'bullet is outbounds!!');
            this.visible = false;
            tcb.Utils.isBullet = false;
            tcb.Utils.isBulletFire = false;
            if (!tcb.Utils.isTween){
                this.stage.bullets.reShow();
            }
        }
    };

    Bullet.prototype.doubleCount = function(){
        this.count *= 2;
    };

    Bullet.prototype.halfCount = function(){
        this.count = Math.floor(this.count / 2);
    };

    /**
     * 升级，增强
     */
    Bullet.prototype.enhance = function(){
        this.power *= 2;
        this.baseSpeed += 500;
    };

    tcb.Bullet = Bullet;
})();


(function () {
    var BulletCount = function(bmp){
        createjs.Container.call(this);

        var bulletCount = new createjs.Bitmap(bmp);
        bulletCount.regX = bmp.width/2;
        bulletCount.regY = bmp.height/2;
        bulletCount.visible = false;
        this.addChild(bulletCount);

        var bulletHint = new createjs.Text('' + this.bulletCount,'38pt verdana','#000');
        bulletHint.y = bulletCount.y;
        bulletHint.regY = bmp.height/2;
        bulletHint.visible = false;
        this.addChild(bulletHint);

        this.count = bulletCount;
        this.hint = bulletHint;
        this.b = null;
    };
    BulletCount.prototype = Object.create(createjs.Container.prototype);
    BulletCount.prototype.constructor = BulletCount;

    BulletCount.prototype.show = function(x,y){
        this.count.x = x;
        this.count.y = this.hint.y = y;
        this.hint.x = x + 80;
        this.count.visible = true;
        this.hint.visible = true;
    };
    BulletCount.prototype.setText = function(text){
        this.hint.text = text;
    };

    BulletCount.prototype.setOnclickBounds = function(func){
        var bounds = this.getBounds();
        this.b = document.createElement('div');
        this.b.className = 'click_bounds';
        this.b.style.left = bounds.x + 'px';
        this.b.style.top = bounds.y + 'px';
        this.b.style.width = bounds.width + 'px';
        this.b.style.height = bounds.height + 'px';
        this.b.onclick = func;
        document.body.appendChild(this.b);
    };

    tcb.BulletCount = BulletCount;
})();


(function () {
    var Bullets = function(){
        createjs.Container.call(this);
        this.baseSpeed = 20;
        this.cacheBullet = null;

        this.stone = null;
        this.gold = null;
        this.shit = null;
        this.s = null;

        this.moveCallback = null;
        this.showCallback = null;
        this.shitInitCallback = function(shit){};

        this.stoneCountUI = null;
        this.goldCountUI = null;
        this.bulletPointer = null;
        this.switchBullet = null;

        this.isFirstBulletEmpty = false;
        this.scx = 0;
        this.gcx = 0;
    };

    Bullets.prototype = Object.create(createjs.Container.prototype);
    Bullets.prototype.constructor = Bullets;
    Bullets.prototype.initChild = function(data){
        var loader = this.stage.loader;
        this.show(data);

        var bmpbc = loader.getResult(loader.ID_BULLET_COUNT);
        var bulletCount = new tcb.BulletCount(bmpbc);
        var scX = 100 * tcb.Utils.scale
        bulletCount.show(scX, tcb.Utils.STAGE_H - 70 * tcb.Utils.scale);
        bulletCount.setText(data.bullet_count);
        this.addChild(bulletCount);
        var that = this;
        bulletCount.setOnclickBounds(function(){
            console.log('stone bullet switch');
            that.switch(false);
        });

        this.stoneCountUI = bulletCount;
        this.scx = scX;

        var bmpgc = loader.getResult(loader.ID_BULLET_GOLD_COUNT);
        var bulletGoldCount = new tcb.BulletCount(bmpgc);
        var gcX = 150 * tcb.Utils.scale + bulletCount.getBounds().width / 2 + bmpgc.width;
        bulletGoldCount.show(gcX, tcb.Utils.STAGE_H - 70 * tcb.Utils.scale);
        bulletGoldCount.setText(data.bullet_gold_count);
        this.addChild(bulletGoldCount);
        bulletGoldCount.setOnclickBounds(function(){
            console.log('gold bullet switch');
            that.switch(true);
        });

        this.goldCountUI = bulletGoldCount;
        this.gcx = gcX;

        var bmpp = loader.getResult(loader.ID_POINTER);
        console.log(bmpp.toString());
        this.bulletPointer = new createjs.Bitmap(bmpp);
        this.bulletPointer.y = tcb.Utils.STAGE_H - 180 * tcb.Utils.scale;
        this.bulletPointer.x = this.scx;
        this.addChild(this.bulletPointer);
    };

    Bullets.prototype.show = function(){
        var loader = this.stage.loader,
            bmp;
        bmp = loader.getResult(loader.ID_BULLET);
        tcb.Utils.BULLET_W = bmp.width * tcb.Utils.scale;
        tcb.Utils.BULLET_H = bmp.height * tcb.Utils.scale;

        tcb.Utils.BULLET_X = tcb.Utils.SLINGSHOT_X;
        tcb.Utils.BULLET_Y = tcb.Utils.SLINGSHOT_Y - 50 * tcb.Utils.scale;

        var stone = new tcb.Bullet(bmp, 'stone', data.bullet_count);
        stone.setTransform(tcb.Utils.BULLET_X, tcb.Utils.BULLET_Y,tcb.Utils.scale,tcb.Utils.scale);
        stone.visible = false;
        this.addChild(stone);
        stone.moveCallback = this.moveCallback;
        this.stone = stone;

        var gbmp = loader.getResult(loader.ID_BULLET_GOLD);
        var gold = new tcb.Bullet(gbmp, 'gold', data.bullet_gold_count);
        gold.setTransform(this.stone.x, this.stone.y, tcb.Utils.scale, tcb.Utils.scale);
        gold.visible = false;
        gold.moveCallback = this.moveCallback;
        this.addChild(gold);
        this.gold = gold;

        //添加一坨屎
        var sbmp = loader.getResult(loader.ID_SHIT);
        var shit = new tcb.Bullet(sbmp, 'shit', 0);
        shit.visible = false;
        this.addChild(shit);
        this.shit = shit;
        this.shitInitCallback(shit);

        this.switchBullet = this.cacheBullet = this.stone;
        this.reShow();
        if (this.showCallback){
            //this.showCallback.c(new Point(bullet.x,bullet.y));
        }

        var that = this;
        var func = function(){
            that.noBullet();
        };

        this.stone.emptyCallback = func;
        this.gold.emptyCallback = func;

        console.log('speed dis ' + (tcb.Utils.BULLET_Y - tcb.Utils.pathFactory.baseY));
        tcb.Utils.speedScale = (tcb.Utils.BULLET_Y - tcb.Utils.pathFactory.baseY) / 1370;
    };
    Bullets.prototype.noBullet = function(){
        if (this.isFirstBulletEmpty) {
            console.log('第22222个子弹空');
            tcb.Utils.noBullets();
        } else {
            console.log('第一个子弹空');
            this.isFirstBulletEmpty = true;
            this.switch(this.cacheBullet == this.stone);
        }
    };
    Bullets.prototype.getShit = function(){
        return this.shit;
    };
    Bullets.prototype.switch = function(isGold){
        if (isGold) {
            if (this.gold.count <= 0) {
                return ;
            }
            this.bulletPointer.x = this.gcx;
            this.switchBullet = this.gold;
        } else {
            if (this.stone.count <= 0) {
                return ;
            }
            this.bulletPointer.x = this.scx;
            this.switchBullet = this.stone;
        }

        if (!tcb.Utils.isBulletFire) {
            this.cacheBullet.visible = false;
            this.reShow();
        }
    };

    Bullets.prototype.reShow = function(force){
        //this.bulletCount--;
        this.cacheBullet = this.switchBullet;
        if (!this.cacheBullet.visible || force) {
            this.cacheBullet.reset();
        }

    };
    Bullets.prototype.getStoneCount = function(){
        return this.stone.count;
    };
    Bullets.prototype.getGoldCount = function(){
        return this.gold.count;
    };
    Bullets.prototype.hasBulletVisile = function(){
        return this.cacheBullet.visible;
    };

    Bullets.prototype.loop = function(evt){

        if (this.cacheBullet != null){
            this.cacheBullet.loop(evt);
        }

        if (this.shit != null) {
            this.shit.loop(evt);
        }

        if (this.stoneCountUI != null) {
            this.stoneCountUI.setText(this.stone.count);
        }

        if (this.goldCountUI != null) {
            this.goldCountUI.setText(this.gold.count);
        }
    };
    Bullets.prototype.down = function(evt){
        if (this.cacheBullet != null) {
            this.cacheBullet.down(evt);
        }
    };
    Bullets.prototype.up = function(evt){
        if (this.cacheBullet != null) {
            return this.cacheBullet.up(evt);
        }

        return false;
    };
    Bullets.prototype.move = function(evt){
        if (this.cacheBullet != null) {
            this.cacheBullet.move(evt);
        }
    };

    Bullets.prototype.enhance = function(){
        console.log('武器升级');
        tcb.Utils.BULLET_X = tcb.Utils.SLINGSHOT_X;
        tcb.Utils.BULLET_Y = tcb.Utils.SLINGSHOT_Y - 20 * tcb.Utils.scale;
        this.cacheBullet.x = tcb.Utils.BULLET_X;
        this.cacheBullet.y = tcb.Utils.BULLET_Y;

        this.reShow(true);
        this.stone.enhance();
        this.gold.enhance();
    };

    Bullets.prototype.setCount = function(bulletCount, goldCount){
        if (typeof(bulletCount) == 'number' && bulletCount >= 0) {
            this.stone.count = bulletCount;
        }

        if (typeof(goldCount) == 'number' && goldCount >= 0) {
            this.gold.count = goldCount;
        }

    };

    tcb.Bullets = Bullets;
})();
this.tcb = this.tcb || {};

(function () {
    /**
     * 云朵类
     * @param bmp 图片
     * @constructor
     */
    var Cloud = function(bmp){
        createjs.Bitmap.call(this, bmp);

        this.w = bmp.width;
        this.h = bmp.height;
        this.regX = this.w / 2;
        this.regY = this.h / 2;

        //this.life = 10;
        this.life = new tcb.Life(10);
        this.life.show();
        this.life.setTarget(this);

        this.speed = 1;
        this.realSpeed = this.speed;
        this.isTween = false;
    };

    Cloud.prototype = Object.create(createjs.Bitmap.prototype);
    Cloud.prototype.constructor = Cloud;

    Cloud.prototype.loop = function(evt){
        //不可见，
        if (!this.visible || this.isTween) {
            return;
        }

        this.x += this.realSpeed;
        //设置血条的位置
        this.setLifePostion();

        //向右，运动
        if (this.realSpeed > 0) {
            if (this.x > tcb.Utils.STAGE_W - this.w / 2) {
                this.realSpeed = -this.speed;
            }
        } else {//向左
            if (this.x < this.w / 2) {
                this.realSpeed = this.speed;
            }
        }
    };

    Cloud.prototype.hit = function(bullet) {
        console.log('overlag cloud');
        bullet.visible = false;
        tcb.Utils.isBullet = false;

        if (!tcb.Utils.isTween){
            this.stage.bullets.reShow();
        }

        this.life.loss(bullet.power,true);
        var alpha = this.life.getRate();
        if (alpha <=0) {
            alpha = 0.1;
        }
        var y = this.y;
        var ty = 5;
        var tween = createjs.Tween.get(this);
        this.isTween = true;
        tween.to({y:y - ty}, 100, createjs.Ease.getPowIn(2))
            .to({y:y + ty}, 200, createjs.Ease.getPowOut(2))
            .to({y:y - ty}, 200, createjs.Ease.getPowIn(2))
            .to({y:y + ty}, 200, createjs.Ease.getPowOut(2))
            .to({alpha: alpha}, 500, createjs.Ease.getPowInOut(2));
        tween.call(this.checkVisible,[], this);
    };

    Cloud.prototype.checkVisible = function(){
        this.isTween = false;
        this.life.showTween();
        if (this.life.isDie()) {
            console.log('cloud is die!!!');
            var result = tcb.Utils.getScroe('cloud',this.stage.bullets.getStoneCount(),this.stage.bullets.getGoldCount());
            //TODO 设置积分 {"isbz":true/false,"score":20}
            if (!result.isbz) {
                this.stage.staticContainer.setScore(result.score);
            }
            var that = this;
            setTimeout(function(){
                console.log('cloud reshow !!');
                that.reShow();
            }, 60 * 1000);
        }
    };

    Cloud.prototype.setLifePostion = function(){
        var x = this.x,
            y = this.y + this.h / 2;
        this.life.setPosition(x, y);
    };

    Cloud.prototype.getB = function(){
        var b = this.getTransformedBounds();
        return b;
    };
    Cloud.prototype.reShow = function(){
        this.x = this.w;
        this.y = tcb.Utils.SLINGSHOT_Y - 500 * tcb.Utils.scale;
        this.visible = true;
        this.alpha = 1;
        this.life.reShow(this.x, this.y);
    };

    tcb.Cloud = Cloud;
})();

(function () {
    /**
     * 云朵容器类
     * @constructor
     */
    var Clouds = function(){
        createjs.Container.call(this);
    };

    Clouds.prototype = Object.create(createjs.Container.prototype);
    Clouds.prototype.constructor = Clouds;

    Clouds.prototype.initChild = function(){
        var loader = this.stage.loader;
        var bmp = loader.getResult(loader.ID_CLOUD);
        var cloud = new tcb.Cloud(bmp);
        cloud.x = tcb.Utils.HALF_W;
        cloud.y = tcb.Utils.SLINGSHOT_Y - 500 * tcb.Utils.scale;
        cloud.setLifePostion();

        this.addChild(cloud);

        console.log("cloud x = " + cloud.x + "cloud y = " + cloud.y);
    };

    Clouds.prototype.loop = function(evt) {
        var N = this.children.length,
            i;
        for (i = 0; i < N; i++) {
            this.getChildAt(i).loop(evt);
        }
    };

    Clouds.prototype.overlap = function(bullet){
        var clouds = this.children,
            N = clouds.length,
            c,
            i;
        //检查子弹，云碰撞
        if (clouds.length > 0) {
            for (i = 0; i < N; i++) {
                c = clouds[i];
                if (bullet.overlap(c)) {
                    c.hit(bullet);
                    return;
                }
            }
        }
    };

    tcb.Clouds = Clouds;
})();
this.tcb = this.tcb || {};

(function () {
    /**
     * 核心游戏舞台
     * @param canvasID
     * @constructor
     */
    var CoreStage = function(canvasID, data){
        createjs.Stage.call(this, canvasID);
        var Utils = tcb.Utils;
        Utils.STAGE_W = this.canvas.width;
        Utils.STAGE_H = this.canvas.height;
        Utils.HALF_W = Utils.STAGE_W / 2;
        Utils.HALF_H = Utils.STAGE_H / 2;

        this.data = data;
        this.loader = new tcb.ResourceLoader();
        this.loader.on('complete',this.loaderComplete,this);
        this.loader.on('progress',this.loaderProgress, this);

        this.staticContainer = new tcb.StaticContainer();
        this.bullets = new tcb.Bullets();
        this.planes = new tcb.Planes();
        this.rubber = new tcb.RubberContainer();
        this.clouds = new tcb.Clouds();

        this.debugShape = new createjs.Shape();
        this.pathShape = new createjs.Shape();

        this.loaddom = new tcb.LoaderDOM();
        this.fps = new createjs.Text("", "36px Arial", "#777");
        this.movetext = new createjs.Text("", "26px Arial", "#777");

        this.loadWidth = tcb.Utils.STAGE_W * 0.7;

        this.preLoad();
        this.loader.startLoad();
    };

    CoreStage.prototype = Object.create(createjs.Stage.prototype);
    CoreStage.prototype.constructor = CoreStage;

    CoreStage.prototype.loaderComplete = function(){
        //this.removeChild(this.loadContainer);
        this.loaddom.destory();

        this.addChild(this.staticContainer);
        this.addChild(this.planes);
        this.addChild(this.bullets);

        this.staticContainer.initChild();
        this.bullets.initChild(this.data);
        this.planes.initChild(this.data);

        this.addChild(this.rubber);
        this.rubber.initChild();

        this.addChild(this.clouds);
        this.clouds.initChild();

        if (tcb.Utils.debugF){
            this.fps.x = tcb.Utils.HALF_W;
            this.fps.y = 50;
            this.addChild(this.fps);
        }

        if (tcb.Utils.isDebug){
            this.movetext.x = 100;
            this.movetext.y = 100;
            this.addChild(this.movetext);
            this.addChild(this.debugShape);
            //this.addChild(this.pathShape);
            //this.debugDrawPath();
        }

        this.update();
    };
    CoreStage.prototype.startLoop = function(){
        createjs.Ticker.setFPS(60);
        createjs.Ticker.on('tick',this.loop,this);
        createjs.Touch.enable(this,true);

        this.on('stagemousedown', this.down, this);
        this.on('stagemouseup', this.up, this);
        this.on('stagemousemove', this.move, this);

        var func = function(p,angle,d){
            this.rubber.drawRubber(p,angle,d);
        };
        this.bullets.moveCallback = new tcb.Callback(func, this);
        this.bullets.showCallback = new tcb.Callback(func, this);
        var that = this;
        this.bullets.shitInitCallback = function(shit) {
            that.planes.initCrow(shit);
        };

        this.rubber.tweenComplete = function(){
            tcb.Utils.isTween = false;
            if (!tcb.Utils.isBullet){
                this.bullets.reShow();
            }

            if (tcb.Utils.isPreEnhance) {
                this.staticContainer.enhance();
            }
        };
        this.rubber.tweenCompleteContext = this;
    };
    CoreStage.prototype.loop = function(evt){
        if (tcb.Utils.pause){
            return;
        }
        this.bullets.loop(evt);
        this.planes.loop(evt);
        this.clouds.loop(evt);
        this.update(evt);

        if (tcb.Utils.debugB){
            this.debug();
        }

        if (tcb.Utils.debugF){
            this.fps.text = 'fps:'+createjs.Ticker.getMeasuredFPS().toFixed(2);
        }

        //if (Utils.isBulletFire){
        this.overlap();
        this.checkOutBounds();
        //}
    };

    CoreStage.prototype.down = function(evt){
        if (evt.stageY < tcb.Utils.STAGE_H*2 / 3){
            return;
        }

        if (evt.stageX < tcb.Utils.SLINGSHOT_X - tcb.Utils.SLINGSHOT_W / 2 || evt.stageX > tcb.Utils.SLINGSHOT_X + tcb.Utils.SLINGSHOT_W / 2) {
            return;
        }

        if(!tcb.Utils.isBulletFire && !tcb.Utils.isScore && this.bullets.hasBulletVisile()){
            this.bullets.down(evt);
        }
    };

    CoreStage.prototype.up = function(evt){
        if (this.bullets.cacheBullet.positionDown.y < tcb.Utils.STAGE_H*2/3){
            return;
        }

        if (this.bullets.cacheBullet.position.y < tcb.Utils.STAGE_H*2/3){
            return;
        }

        if (!tcb.Utils.isBulletFire && !tcb.Utils.isScore){
            if (this.bullets.up(evt)) {
                this.rubber.up(evt);
            }
        }
    };

    CoreStage.prototype.move = function(evt){
        if (!tcb.Utils.isBulletFire && !tcb.Utils.isScore){
            this.bullets.move(evt);
        }
    };

    /**
     * 检查是否碰撞
     */
    CoreStage.prototype.overlap = function(){
        var a = this.bullets.cacheBullet,
            planes = this.planes.children,
            clouds = this.clouds.children,
            i,
            p,
            c;

        if (a == null || planes == null) {
            return;
        }

        this.staticContainer.overlap(this.bullets.getShit());

        if (this.planes.isCrazy()) {
            this.staticContainer.overlap(this.planes.getCrazy());
        }

        //检查子弹，云碰撞
        this.clouds.overlap(a);

        //检查子弹，飞机碰撞
        this.planes.overlap(a);
    };

    /**
     * 检查子弹是否出界
     */
    CoreStage.prototype.checkOutBounds = function(){
        var b = this.bullets.cacheBullet;
        if (b == null || !b.visible){
            return;
        }
        b.checkOutBounds();
    };

    /**
     * debug画出飞机路线和碰撞边界
     */
    CoreStage.prototype.debug = function(){
        var g = this.debugShape.graphics;
        g.clear();
        g.setStrokeStyle(1, 'round', 'round');
        g.beginStroke("#F0F");

        var planes = this.planes.children,
            i,
            p;

        if (planes == null) {
            return;
        }

        if (Array.isArray(planes) && planes.length > 0){
            for (i = 0; i < planes.length; i++){
                p = planes[i];
                if (p.visible){
                    g.append(tcb.Utils.rectangleToRect(p.getB()));
                }
            }
        }

        if (this.bullets.cacheBullet != null && this.bullets.cacheBullet.visible) {
            g.append(tcb.Utils.rectangleToRect(this.bullets.cacheBullet.getB()));
        }

        //g.beginStroke('#01DF01');
        //this.planes.drawPath(g);
        //g.rect(Utils.STAGE_W/2,0,1,Utils.STAGE_H);
        //
        //g.beginStroke('#DF013A');
        //g.rect(Utils.SLINGSHOT_X,Utils.SLINGSHOT_Y,3,3);
    };

    CoreStage.prototype.debugDrawPath = function(){
        var g = this.pathShape.graphics;
        g.setStrokeStyle(1, 'round', 'round');

        g.beginStroke('#01DF01');
        this.planes.drawPath(g);
        g.rect(tcb.Utils.STAGE_W/2,0,1,tcb.Utils.STAGE_H);

        g.beginStroke('#DF013A');
        g.rect(tcb.Utils.SLINGSHOT_X,tcb.Utils.SLINGSHOT_Y,3,3);
    };

    /**
     * 核心舞台，初始化完毕，准备，加载资源
     */
    CoreStage.prototype.preLoad = function(){
        this.loaddom.add();
    };

    /**
     * 资源加载中
     * @param evt
     */
    CoreStage.prototype.loaderProgress = function(evt){
        var width = evt.loaded * (this.loadWidth-30) + 30;
        this.loaddom.progress(width);
    };

    tcb.CoreStage = CoreStage;
})();

var stage,data;
function startGame(){
    data = tcb.Utils.getData();
    if (!tcb.Utils.resizeCanvas('game_canvas')){
        //alert("暂时不支持该机型。");
    }
    stage = new tcb.CoreStage('game_canvas', data);
    //stage.loader.startLoad();
    stage.startLoop();
}

function resumeGame(){
    tcb.Utils.isScore = false;
    //if (stage.bullets.bulletCount > 0){
    //    //Utils.pause = false;
    //} else {
    //    Utils.noBullets();
    //}
}

if(DEBUG){
    window.onload = function(){
        startGame();
    };
}

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