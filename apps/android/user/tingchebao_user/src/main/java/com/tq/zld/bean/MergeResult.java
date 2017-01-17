package com.tq.zld.bean;

/**
 * Created by GT on 2015/9/19.
 */
public class MergeResult {
    public String errmsg;
    public String result;//-1双方失败，1成功，-3
    public Ret ownret;
    public Ret friendret;
    public Ticket ownticket;
    public Ticket friendticket;

    public class Ret{
        public String buttip;
        public String imgurl;
        public String righttip;
        public String toptip;
        public String win;

        @Override
        public String toString() {
            return "Ret{" +
                    "buttip='" + buttip + '\'' +
                    ", imgurl='" + imgurl + '\'' +
                    ", righttip='" + righttip + '\'' +
                    ", toptip='" + toptip + '\'' +
                    ", win='" + win + '\'' +
                    '}';
        }
    }

    public class Ticket{
        public String money;
        public String name;
        public int isbuy;

        @Override
        public String toString() {
            return "Ticket{" +
                    "money='" + money + '\'' +
                    ", name='" + name + '\'' +
                    ", isbuy='" + isbuy + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MergeResult{" +
                "errmsg='" + errmsg + '\'' +
                ", result='" + result + '\'' +
                ", ownret=" + ownret +
                ", friendret=" + friendret +
                ", ownticket=" + ownticket +
                ", friendticket=" + friendticket +
                '}';
    }
}
