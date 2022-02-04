package hwnet.survivalgames.handlers;

import java.util.HashMap;

public class VoteHandler {

    private static HashMap<String, Map> votes = new HashMap<String, Map>();

    public static void vote(String uname, Map mapp) {
        if (!hasVoted(uname)) {
            votes.put(uname, mapp);
            System.out.println("User put in hashmap");
        } else {
            System.out.println("User already in hashmap");
        }
    }

    public static boolean hasVoted(String p) {
        return votes.containsKey(p);
    }

    public static Map getVotedMap(String p) {
        return votes.get(p);
    }

    public static int getVotesMap(Map map) {
        int out = 0;
        for (String pl : votes.keySet()) {
            Map vote = votes.get(pl);
            if (vote == map) {
                out++;
            }
        }
        return out;
    }

    public static Map getWithMostVotes() {
        HashMap<Map, Integer> amounts = new HashMap<Map, Integer>();
        for (String s : votes.keySet()) {
            Map m = votes.get(s);
            if (amounts.containsKey(m))
                amounts.put(m, amounts.get(m) + 1);
            else
                amounts.put(m, 1);
        }
        int most = 0;
        Map mapmost = null;
        for (Map m : amounts.keySet()) {
            int v = amounts.get(m);
            if (v > most) {
                most = v;
                mapmost = m;
            }
        }
        return mapmost;
    }
}