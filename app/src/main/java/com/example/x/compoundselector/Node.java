package com.example.x.compoundselector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Richa on 05/09/2017.
 */
// TODO: 08/09/2017 NullNode mode
public class Node {

    public static final List<Node> EMPTY_NODE_CHILD = Collections.emptyList();

    private String showName;
    private String value;
    private Object tag;

    private Node parent;
    private List<Node> children = new ArrayList<>();

    private boolean checked;

    public Node(String showName, String value) {
        this(showName, value, null);
    }

    public Node(String showName, String value, Object tag) {
        this.showName = showName;
        this.value = value;
        this.tag = tag;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children == null || children.size() == 0;
    }

    public int getHeight() {
        return isRoot() ? 0 : (parent.getHeight() + 1);
    }

    public boolean addChild(Node node) {
        if (!children.contains(node)) {
            children.add(node);
            node.parent = this;
            return true;
        } else {
            return false;
        }
    }

    public boolean removeChild(Node node) {
        if (!children.contains(node)) {
            return false;
        }
        node.parent = null;
        return children.remove(node);
    }

    //region Utility function

    /**
     * Return <tt>true</tt> if the specified node element is root node.
     * This is a null-safe version of the non-static method {@code isRoot()}.
     *
     * @param node the node element to be tested.
     * @return Return <tt>true</tt> if the specified node element is root node.
     */
    public static boolean isRoot(Node node) {
        return node != null && node.isRoot();
    }

    /**
     * Returns the root node of the tree which contains the specified node {@code node}.
     *
     * @param node the node element that marks a tree.
     * @return the root node of the tree.
     */
    public static Node getRoot(Node node) {
        Node tempNode = node;
        while (tempNode != null) {
            tempNode = tempNode.getParent();
        }
        return tempNode;
    }

    /**
     * Returns <tt>true</tt> if the node is a branch node (non-leaf and non-null).
     *
     * @param node the node to be tested.
     * @return Returns <tt>true</tt> if the node is a branch node.
     */
    private static boolean isBranchNode(Node node) {
        return (node != null && node.children != null && node.children.size() > 0);
    }

    /**
     * Returns the simple path which contains all the nodes from the root to the specified node.
     *
     * @param node the target node.
     */
    public static List<Node> getNodeSimplePath(final Node node) {
        LinkedList<Node> nodePath = new LinkedList<>();
        Node tempNode = node;
        while (tempNode != null) {
            nodePath.addFirst(tempNode);
            tempNode = tempNode.parent;
        }
        return nodePath;
    }

    /**
     * Returns the degree of the tree. {@code null} or single node tree would be thought as zero degree.
     *
     * @param rootNode the root of the tree.
     * @return Returns the degree of the tree.
     */
    public static int getTreeDegree(final Node rootNode) {
        if (!isBranchNode(rootNode)) {
            return 0;
        }
        int degree = rootNode.children.size();
        for (Node child : rootNode.children) {
            if (!child.isLeaf()) {
                degree = Math.max(degree, getTreeDegree(child));
            }
        }
        return degree;
    }

    /**
     * Returns the depth of the tree.
     *
     * @param rootNode the root of the tree.
     * @return the tree's depth.
     */
    public static int getTreeDepth(Node rootNode) {
        depth = 0;
        if (rootNode == null) {
            return 0;
        }
        dfs(rootNode, 1);
        return depth;
    }

    private static int depth;

    private static void dfs(Node node, int deep) {
        if (node.children.size() == 0) {
            depth = Math.max(depth, deep);
        } else {
            for (Node childNode : node.children) {
                dfs(childNode, deep + 1);
            }
        }
    }

    /**
     * Searches the checked leaf node from the tree.
     *
     * @param rootNode the root of the tree.
     * @return the checked leaf node, if exist. Or {@code null}
     * if all the leaf nodes in the tree are unchecked.
     */
    public static Node findCheckedLeafPreOrder(Node rootNode) {
        return iterativePreOrder(rootNode, new DataAction() {
            @Override
            public Node action(Node node) {
                return (node.isLeaf() && node.isChecked()) ? node : null;
            }
        });
    }

    /**
     * Sets all the tree nodes' states to be unchecked state.
     *
     * @param rootNode the root of the tree.
     */
    public static void setAllNodeUnchecked(Node rootNode) {
        iterativePreOrder(rootNode, new Action() {
            @Override
            public void action(Node node) {
                node.checked = false;
            }
        });
    }

    /**
     * Checks that if the tree contains the specified node element.
     *
     * @param rootNode   the root of the tree.
     * @param targetNode note element whose presence in the tree is to be tested
     * @return the node in the tree equals to the specified node, or {@code null} if the tree
     * does not contain the specified node.
     */
    public static Node findNode(Node rootNode, final Node targetNode) {
        if (rootNode == null || targetNode == null) {
            return null;
        }
        return iterativePreOrder(rootNode, new DataAction() {
            @Override
            public Node action(Node node) {
                return node.equals(targetNode) ? node : null;
            }
        });
    }

    /**
     * Sets the specified note to be checked state, including all its parent nodes. And
     * uncheck all the others.
     * <p>If {@code leafNode} is not a leaf node, or does not belong to the tree {@code rootNode},
     * nothing would happen.</p>
     *
     * @param rootNode the root of the tree.
     * @param leafNode the leaf node wanner be set checked.
     * @return returns <tt>true</tt> if the operation succeeds, or <tt>false</tt> if the param
     * {@code leafNode} is not a leaf node or it is not a node element of the tree {@code rootNode}.
     */
    public static boolean setSingleLeafNodeChecked(Node rootNode, Node leafNode) {
        return setSingleLeafNodeChecked(rootNode, leafNode, true);
    }

    /**
     * Sets the specified note to be checked state, including all its parent nodes. And
     * uncheck all the others.
     * <p>If {@code leafNode} is not a leaf node, or does not belong to the tree {@code rootNode},
     * nothing would happen.</p>
     *
     * @param rootNode             the root of the tree.
     * @param leafNode             the leaf node wanner be set checked.
     * @param includeCheckRequired whether checking the ownership relationships or not.
     * @return returns <tt>true</tt> if the operation succeeds, or <tt>false</tt> if the param
     * {@code leafNode} is not a leaf node or it is not a node element of the tree {@code rootNode}.
     */
    private static boolean setSingleLeafNodeChecked(Node rootNode, Node leafNode, boolean includeCheckRequired) {
        if (leafNode == null || !(leafNode.isLeaf())) {
            return false;
        }

        Node nodeElement;
        if (!includeCheckRequired) {
            nodeElement = leafNode;
        } else {
            nodeElement = findNode(rootNode, leafNode);
            if (nodeElement == null) {
                return false;
            }
        }
        setAllNodeUnchecked(rootNode);
        Node node = nodeElement;
        while (node != null) {
            node.checked = true;
            node = node.parent;
        }
        return true;
    }

    /**
     * Returns the first leaf node element of this tree. Based on the sequence of preorder traversal.
     */
    public Node getFirstLeafPreOrder() {
        if (isLeaf()) {
            return this;
        }
        return iterativePreOrder(this, new DataAction() {
            @Override
            public Node action(Node node) {
                return node.isLeaf() ? node : null;
            }
        });
    }

    /**
     * Returns the checked leaf node of the tree. If no such node found, the first leaf node
     * of the tree and all its parents would be set to be checked.
     * <p> A tree with correct state should have no checked leaf node or one at least.
     *
     * @param rootNode the root of the tree.
     * @return Returns the checked leaf node of the tree.
     */
    public static Node getCheckedLeafWithDefault(Node rootNode) {
        if (rootNode == null) {
            return null;
        }
        Node checkedLeafNode = Node.findCheckedLeafPreOrder(rootNode);
        if (checkedLeafNode == null) {
            checkedLeafNode = rootNode.getFirstLeafPreOrder();
            Node leafNode = checkedLeafNode;
            Node.setSingleLeafNodeChecked(rootNode, leafNode, false);
        }
        return checkedLeafNode;
    }

    /**
     * Preorder traversal the tree and performs the given action for each node element.
     *
     * @param rootNode the root of the tree.
     * @param action   the action to apply to every node when traversing. Without return value.
     */
    private static void iterativePreOrder(final Node rootNode, Action action) {
        if (rootNode == null || action == null) {
            return;
        }
        Stack<Node> nodeStack = new Stack<>();
        nodeStack.push(rootNode);
        while (!nodeStack.isEmpty()) {
            Node item = nodeStack.pop();
            action.action(item);
            // right child is pushed first so that left is processed first
            for (int i = item.children.size() - 1; i >= 0; i--) {
                nodeStack.push(item.children.get(i));
            }
        }
    }

    /**
     * Returns the node which matches the rule defined int action.
     *
     * @param rootNode the root of the tree.
     * @param action   the action to apply to every node for filtering.
     * @return the node matches the rule defined in action.
     */
    private static Node iterativePreOrder(final Node rootNode, DataAction action) {
        if (rootNode == null || action == null) {
            return null;
        }
        Stack<Node> nodeStack = new Stack<>();
        nodeStack.push(rootNode);
        while (!nodeStack.isEmpty()) {
            Node item = nodeStack.pop();
            Node node = action.action(item);
            if (node != null) {
                return node;
            }
            // right child is pushed first so that left is processed first
            for (int i = item.children.size() - 1; i >= 0; i--) {
                nodeStack.push(item.children.get(i));
            }
        }
        return null;
    }

    /**
     * Formats the tree.
     *
     * @param rootNode the root of the tree.
     */
    public static void formatTree(final Node rootNode) {
        if (rootNode == null) {
            return;
        }
        List<Node> children = rootNode.getChildren();
        if (children == null || children.size() == 0) {
            return;
        }
        for (Node node : children) {
            node.parent = rootNode;
            if (node.children == null) {
                node.children = EMPTY_NODE_CHILD;
            } else if (node.children.size() > 0) {
                formatTree(node);
            }
        }
    }

    private interface Action {
        void action(Node node);
    }

    private interface DataAction {
        Node action(Node node);
    }
    //endregion

    //region equals & hashcode & clone & toString, all based on the showName and value field.
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Node node = (Node) o;
        if (!showName.equals(node.showName))
            return false;
        return value != null ? value.equals(node.value) : node.value == null;
    }

    @Override
    public int hashCode() {
        int result = showName.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    protected Object clone() {
        return new Node(showName, value);
    }

    @Override
    public String toString() {
        return "Node{" + "showName='" + showName + '\'' + ", value='" + value + '\'' + '}';
    }

    //endregion

    //region Getter & Setter
    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children == null ? null : Collections.unmodifiableList(children);
    }

    public boolean isChecked() {
        return checked;
    }

    //endregion

}
