package org.drools.reteoo;
/*
 * Copyright 2005 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.FactException;
import org.drools.rule.And;
import org.drools.rule.InvalidPatternException;
import org.drools.rule.Rule;
import org.drools.spi.ClassObjectTypeResolver;
import org.drools.spi.ObjectTypeResolver;
import org.drools.spi.ObjectType;
import org.drools.spi.PropagationContext;

/**
 * The Rete-OO network.
 * 
 * The Rete class is the root <code>Object</code>. All objects are asserted into
 * the Rete node where it propagates to all matching ObjectTypeNodes.
 * 
 * The first time an  instance of a Class type is asserted it does a full
 * iteration of all ObjectTyppeNodes looking for matches, any matches are 
 * then cached in a HashMap which is used for future assertions.
 * 
 * While Rete  extends ObjectSource nad implements ObjectSink it nulls the 
 * methods attach(), remove() and  updateNewNode() as this is the root node
 * they are no applicable
 * 
 * @see ObjectTypeNode
 * 
 * @author <a href="mailto:mark.proctor@jboss.com">Mark Proctor</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 */
class Rete extends ObjectSource
    implements
    Serializable,
    ObjectSink,
    NodeMemory {
    // ------------------------------------------------------------
    // Instance members
    // ------------------------------------------------------------

    /** The <code>Map</code> of <code>ObjectTypeNodes</code>. */
    private final Map                objectTypeNodes = new HashMap();
    
    private final ObjectTypeResolver resolver;       

    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------

    /**
     * Construct.
     */
    public Rete() {
        this( null );
    }
    
    public Rete(ObjectTypeResolver resolver) {
        super( 0 );
        this.resolver = resolver;
    }
    // ------------------------------------------------------------
    // Instance methods
    // ------------------------------------------------------------

    

    /**
     * This is the entry point into the network for all asserted Facts. Iterates a cache
     * of matching <code>ObjectTypdeNode</code>s asserting the Fact. If the cache does not
     * exist it first iteraes and builds the cache.
     * 
     * @param handle
     *            The FactHandle of the fact to assert
     * @param context
     *            The <code>PropagationContext</code> of the <code>WorkingMemory</code> action   
     * @param workingMemory
     *            The working memory session.
     */
    public void assertObject(FactHandleImpl handle,
                             PropagationContext context,
                             WorkingMemoryImpl workingMemory) {
        HashMap memory = (HashMap) workingMemory.getNodeMemory( this );

        Object object = handle.getObject();

        ObjectTypeNode[] cachedNodes = (ObjectTypeNode[]) memory.get( object.getClass() );
        if ( cachedNodes == null ) {
            cachedNodes = getMatchingNodes( object );
            memory.put( object.getClass(),
                        cachedNodes );
        }

        for ( int i = 0, length = cachedNodes.length; i < length; i++ ) {
            cachedNodes[i].assertObject( handle,
                                         context,
                                         workingMemory );
        }
    }

    /**
     * Retract a fact object from this <code>RuleBase</code> and the specified
     * <code>WorkingMemory</code>.
     * 
     * @param handle
     *            The handle of the fact to retract.
     * @param workingMemory
     *            The working memory session.
     */
    public void retractObject(FactHandleImpl handle,
                              PropagationContext context,
                              WorkingMemoryImpl workingMemory) {
        HashMap memory = (HashMap) workingMemory.getNodeMemory( this );

        Object object = handle.getObject();

        ObjectTypeNode[] cachedNodes = (ObjectTypeNode[]) memory.get( object.getClass() );
        if ( cachedNodes == null ) {
            cachedNodes = getMatchingNodes( object );
            memory.put( object.getClass(),
                        cachedNodes );
        }

        for ( int i = 0; i < cachedNodes.length; i++ ) {
            cachedNodes[i].retractObject( handle,
                                          context,
                                          workingMemory );
        }
    }
    
    public void modifyObject(FactHandleImpl handle,
                             PropagationContext context,
                             WorkingMemoryImpl workingMemory) {
        HashMap memory = (HashMap) workingMemory.getNodeMemory( this );

        Object object = handle.getObject();

        ObjectTypeNode[] cachedNodes = (ObjectTypeNode[]) memory.get( object.getClass() );
        if ( cachedNodes == null ) {
            cachedNodes = getMatchingNodes( object );
            memory.put( object.getClass(),
                        cachedNodes );
        }

        for ( int i = 0; i < cachedNodes.length; i++ ) {
            cachedNodes[i].modifyObject( handle,
                                          context,
                                          workingMemory );
        }
    }    

    private ObjectTypeNode[] getMatchingNodes(Object object) throws FactException {
        List cache = new ArrayList();

        for ( Iterator it = objectTypeNodeIterator(); it.hasNext(); ) {
            ObjectTypeNode node = (ObjectTypeNode) it.next();
            if ( node.matches( object ) ) {
                cache.add( node );
            }
        }
        return (ObjectTypeNode[]) cache.toArray( new ObjectTypeNode[cache.size()] );
    }

    /**
     * Retrieve all <code>ObjectTypeNode</code> children of this node.
     * 
     * @return The <code>Set</code> of <code>ObjectTypeNodes</code>.
     */
    Collection getObjectTypeNodes() {
        return this.objectTypeNodes.values();
    }

    /**
     * Retrieve an <code>Iterator</code> over the <code>ObjectTypeNode</code>
     * children of this node.
     * 
     * @return An <code>Iterator</code> over <code>ObjectTypeNodes</code>.
     */
    Iterator objectTypeNodeIterator() {
        return this.objectTypeNodes.values().iterator();
    }

    /**
     * Retrieve an <code>ObjectTypeNode</code> keyed by
     * <code>ObjectType</code>.
     * 
     * @param objectType
     *            The <code>ObjectType</code> key.
     * 
     * @return The matching <code>ObjectTypeNode</code> if one has already
     *         been created, else <code>null</code>.
     */
    ObjectTypeNode getObjectTypeNode(ObjectType objectType) {
        return (ObjectTypeNode) this.objectTypeNodes.get( objectType );
    }

    /**
     * Add an <code>ObjectTypeNode</code> child to this <code>Rete</code>.
     * 
     * @param node
     *            The node to add.
     */
    private void addObjectTypeNode(ObjectTypeNode node) {
        this.objectTypeNodes.put( node.getObjectType(),
                                  node );
    }

    /**
     * Adds the <code>TupleSink</code> so that it may receive
     * <code>Tuples</code> propagated from this <code>TupleSource</code>.
     * 
     * @param tupleSink
     *            The <code>TupleSink</code> to receive propagated
     *            <code>Tuples</code>.
     */
    protected void addObjectSink(ObjectSink objectSink) {
        addObjectTypeNode( (ObjectTypeNode) objectSink );
    }
    
    protected void removeObjectSink(ObjectSink objectSink) {
        this.objectTypeNodes.remove( objectSink );        
    }    

    public void attach() {
        // do nothing this is the root node
    }
    
    public void attach(WorkingMemoryImpl[] workingMemories, PropagationContext context) {
        // do nothing this is the root node        
    }       

    public void updateNewNode(WorkingMemoryImpl workingMemory,
                              PropagationContext context) {
        // do nothing this has no data to propagate
    }

    public void remove(BaseNode node,
                       WorkingMemoryImpl workingMemory,
                       PropagationContext context) {
        ObjectTypeNode objectTypeNode = ( ObjectTypeNode ) node;
        removeObjectSink( objectTypeNode );
        //@todo: we really should attempt to clear the memory cache for this ObjectTypeNode        
    }

    public Object createMemory() {
        return new HashMap();
    }

}
