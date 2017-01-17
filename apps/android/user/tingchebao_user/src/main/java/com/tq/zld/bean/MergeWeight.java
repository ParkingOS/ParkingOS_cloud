package com.tq.zld.bean;

/**
 * Created by Gecko on 2015/10/13.
 */
public class MergeWeight {
    public String errmsg;
    public int result;
    public String winrate;
    public Weight own;
    public Weight friend;

    public class Weight{
        public int buyvalue;
        public int expvalue;
        public int ticketvalue;
        public int uiontotal;

        @Override
        public String toString() {
            return "Weight{" +
                    "buyvalue=" + buyvalue +
                    ", expvalue=" + expvalue +
                    ", ticketvalue=" + ticketvalue +
                    ", uiontotal=" + uiontotal +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MergeWeight{" +
                "errmsg='" + errmsg + '\'' +
                ", result=" + result +
                ", winrate='" + winrate + '\'' +
                ", own=" + own +
                ", friend=" + friend +
                '}';
    }
}
