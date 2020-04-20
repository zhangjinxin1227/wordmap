package com.zjx.model;

import java.util.*;

class Children {
	private List list = new ArrayList();

	public int getSize() {
		return list.size();
	}

	public void addChild(Node2 node) {
		list.add(node);
	}

	// 拼接孩子节点的JSON字符串
	public String toString() {
		String result = "[";
		for (Iterator it = list.iterator(); it.hasNext();) {
			result += ((Node2) it.next()).toString();
			result += ",";
		}
		result = result.substring(0, result.length() - 1);
		result += "]";
		return result;
	}

	// 孩子节点排序
	public void sortChildren() {
		// 对本层节点进行排序
		// 可根据不同的排序属性，传入不同的比较器，这里传入ID比较器
		Collections.sort(list, new NodeIDComparator());
		// 对每个节点的下一层节点进行排序
		for (Iterator it = list.iterator(); it.hasNext();) {
			((Node2) it.next()).sortChildren();
		}
	}
}

/**
 * 节点比较器
 */
class NodeIDComparator implements Comparator {
	// 按照节点编号比较
	public int compare(Object o1, Object o2) {
		int j1 = Integer.parseInt(((Node2) o1).id);
		int j2 = Integer.parseInt(((Node2) o2).id);
		return (j1 < j2 ? -1 : (j1 == j2 ? 0 : 1));
	}
}