package com.sqrt4.jircd.model.cmd;

import java.util.LinkedList;

public class CommandParameters {
    private LinkedList<String> params = new LinkedList<String>();

    public CommandParameters(String p) {
        int idx, lidx = 0;
        while (true) {
            idx = p.indexOf(' ', lidx);
            if (idx == -1) {
                if (p.length() != lidx) {
                    String param = p.substring(lidx);
                    if (param.charAt(0) == ':')
                        param = param.substring(1); // <trailing>
                    params.add(param);
                }
                break;
            } else {
                String param = p.substring(lidx, idx);
                if(param.charAt(0) == ':') {
                    params.add(p.substring(lidx + 1)); // <trailing>
                    break;
                }
                params.add(param);
                lidx = idx + 1;
            }
        }
    }

    public int size() {
        return params.size();
    }

    public String peek() {
        return params.peek();
    }

    public String peek(int count) {
        StringBuilder sb = new StringBuilder();
        int e = Math.min(count, params.size());
        for (int i = 0; i < e; i++) {
            if (i != 0)
                sb.append(" ");
            sb.append(params.get(i));
        }
        return sb.toString();
    }

    public String poll() {
        return params.poll();
    }

    public String poll(int count) {
        StringBuilder sb = new StringBuilder();
        int e = Math.min(count, params.size());
        for (int i = 0; i < e; i++) {
            if (i != 0)
                sb.append(" ");
            sb.append(params.poll());
        }
        return sb.toString();
    }

    public String remaining() {
        return poll(size());
    }
}