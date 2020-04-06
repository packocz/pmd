/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.table.coreimpl;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.util.OptionalBool;

class ShadowChainNode<S, I> implements ShadowChain<S, I> {

    protected final NameResolver<S> resolver;
    protected final @NonNull ShadowChain<S, I> parent;
    private final boolean shadowBarrier;
    private final I scopeTag;

    ShadowChainNode(@NonNull ShadowChain<S, I> parent,
                    boolean shadowBarrier,
                    I scopeTag,
                    NameResolver<S> resolver) {
        this.parent = parent;
        this.scopeTag = scopeTag;
        this.shadowBarrier = shadowBarrier;
        this.resolver = resolver;
    }

    @Override
    public final boolean isShadowBarrier() {
        return shadowBarrier;
    }

    @Override
    public @NonNull ShadowChain<S, I> getParent() {
        return parent;
    }

    /**
     * This is package protected, because it would be impossible to find
     * a value for this on the root node. Instead, the scope tag
     * is only accessible from a {@link ShadowChainIterator}, if we found
     * results (which naturally excludes the root group, being empty)
     */
    I getScopeTag() {
        return scopeTag;
    }

    @Override
    public ShadowChainIterator<S, I> iterateResults(String name) {
        return new ShadowChainIteratorImpl<>(this, name);
    }

    /** Doesn't ask the parents. */
    protected OptionalBool knowsSymbol(String simpleName) {
        return resolver.knows(simpleName);
    }

    @Override
    public @NonNull List<S> resolve(String name) {
        List<S> res = resolver.resolveHere(name);
        handleResolverKnows(name, !res.isEmpty());
        if (res.isEmpty()) {
            return parent.resolve(name);
        } else if (!isShadowBarrier()) {
            // A successful search ends on the first node that is a
            // shadow barrier, inclusive
            // A failed search continues regardless
            return ConsList.cons(res, parent.resolve(name));
        }
        return res;
    }

    protected void handleResolverKnows(String name, boolean resolverKnows) {
        // to be overridden
    }

    @Override
    public S resolveFirst(String name) {
        S s = resolver.resolveFirst(name);
        handleResolverKnows(name, name != null);
        return s != null ? s : parent.resolveFirst(name);
    }

    @Override
    public String toString() {
        return scopeTag + "  " + resolver.toString();
    }

}
