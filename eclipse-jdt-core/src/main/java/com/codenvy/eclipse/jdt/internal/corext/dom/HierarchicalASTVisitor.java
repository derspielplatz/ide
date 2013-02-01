/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.codenvy.eclipse.jdt.internal.corext.dom;

import com.codenvy.eclipse.jdt.core.dom.ASTNode;
import com.codenvy.eclipse.jdt.core.dom.ASTVisitor;
import com.codenvy.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import com.codenvy.eclipse.jdt.core.dom.Annotation;
import com.codenvy.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import com.codenvy.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import com.codenvy.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import com.codenvy.eclipse.jdt.core.dom.ArrayAccess;
import com.codenvy.eclipse.jdt.core.dom.ArrayCreation;
import com.codenvy.eclipse.jdt.core.dom.ArrayInitializer;
import com.codenvy.eclipse.jdt.core.dom.ArrayType;
import com.codenvy.eclipse.jdt.core.dom.AssertStatement;
import com.codenvy.eclipse.jdt.core.dom.Assignment;
import com.codenvy.eclipse.jdt.core.dom.Block;
import com.codenvy.eclipse.jdt.core.dom.BlockComment;
import com.codenvy.eclipse.jdt.core.dom.BodyDeclaration;
import com.codenvy.eclipse.jdt.core.dom.BooleanLiteral;
import com.codenvy.eclipse.jdt.core.dom.BreakStatement;
import com.codenvy.eclipse.jdt.core.dom.CastExpression;
import com.codenvy.eclipse.jdt.core.dom.CatchClause;
import com.codenvy.eclipse.jdt.core.dom.CharacterLiteral;
import com.codenvy.eclipse.jdt.core.dom.ClassInstanceCreation;
import com.codenvy.eclipse.jdt.core.dom.Comment;
import com.codenvy.eclipse.jdt.core.dom.CompilationUnit;
import com.codenvy.eclipse.jdt.core.dom.ConditionalExpression;
import com.codenvy.eclipse.jdt.core.dom.ConstructorInvocation;
import com.codenvy.eclipse.jdt.core.dom.ContinueStatement;
import com.codenvy.eclipse.jdt.core.dom.DoStatement;
import com.codenvy.eclipse.jdt.core.dom.EmptyStatement;
import com.codenvy.eclipse.jdt.core.dom.EnhancedForStatement;
import com.codenvy.eclipse.jdt.core.dom.EnumConstantDeclaration;
import com.codenvy.eclipse.jdt.core.dom.EnumDeclaration;
import com.codenvy.eclipse.jdt.core.dom.Expression;
import com.codenvy.eclipse.jdt.core.dom.ExpressionStatement;
import com.codenvy.eclipse.jdt.core.dom.FieldAccess;
import com.codenvy.eclipse.jdt.core.dom.FieldDeclaration;
import com.codenvy.eclipse.jdt.core.dom.ForStatement;
import com.codenvy.eclipse.jdt.core.dom.IfStatement;
import com.codenvy.eclipse.jdt.core.dom.ImportDeclaration;
import com.codenvy.eclipse.jdt.core.dom.InfixExpression;
import com.codenvy.eclipse.jdt.core.dom.Initializer;
import com.codenvy.eclipse.jdt.core.dom.InstanceofExpression;
import com.codenvy.eclipse.jdt.core.dom.Javadoc;
import com.codenvy.eclipse.jdt.core.dom.LabeledStatement;
import com.codenvy.eclipse.jdt.core.dom.LineComment;
import com.codenvy.eclipse.jdt.core.dom.MarkerAnnotation;
import com.codenvy.eclipse.jdt.core.dom.MemberRef;
import com.codenvy.eclipse.jdt.core.dom.MemberValuePair;
import com.codenvy.eclipse.jdt.core.dom.MethodDeclaration;
import com.codenvy.eclipse.jdt.core.dom.MethodInvocation;
import com.codenvy.eclipse.jdt.core.dom.MethodRef;
import com.codenvy.eclipse.jdt.core.dom.MethodRefParameter;
import com.codenvy.eclipse.jdt.core.dom.Modifier;
import com.codenvy.eclipse.jdt.core.dom.Name;
import com.codenvy.eclipse.jdt.core.dom.NormalAnnotation;
import com.codenvy.eclipse.jdt.core.dom.NullLiteral;
import com.codenvy.eclipse.jdt.core.dom.NumberLiteral;
import com.codenvy.eclipse.jdt.core.dom.PackageDeclaration;
import com.codenvy.eclipse.jdt.core.dom.ParameterizedType;
import com.codenvy.eclipse.jdt.core.dom.ParenthesizedExpression;
import com.codenvy.eclipse.jdt.core.dom.PostfixExpression;
import com.codenvy.eclipse.jdt.core.dom.PrefixExpression;
import com.codenvy.eclipse.jdt.core.dom.PrimitiveType;
import com.codenvy.eclipse.jdt.core.dom.QualifiedName;
import com.codenvy.eclipse.jdt.core.dom.QualifiedType;
import com.codenvy.eclipse.jdt.core.dom.ReturnStatement;
import com.codenvy.eclipse.jdt.core.dom.SimpleName;
import com.codenvy.eclipse.jdt.core.dom.SimpleType;
import com.codenvy.eclipse.jdt.core.dom.SingleMemberAnnotation;
import com.codenvy.eclipse.jdt.core.dom.SingleVariableDeclaration;
import com.codenvy.eclipse.jdt.core.dom.Statement;
import com.codenvy.eclipse.jdt.core.dom.StringLiteral;
import com.codenvy.eclipse.jdt.core.dom.SuperConstructorInvocation;
import com.codenvy.eclipse.jdt.core.dom.SuperFieldAccess;
import com.codenvy.eclipse.jdt.core.dom.SuperMethodInvocation;
import com.codenvy.eclipse.jdt.core.dom.SwitchCase;
import com.codenvy.eclipse.jdt.core.dom.SwitchStatement;
import com.codenvy.eclipse.jdt.core.dom.SynchronizedStatement;
import com.codenvy.eclipse.jdt.core.dom.TagElement;
import com.codenvy.eclipse.jdt.core.dom.TextElement;
import com.codenvy.eclipse.jdt.core.dom.ThisExpression;
import com.codenvy.eclipse.jdt.core.dom.ThrowStatement;
import com.codenvy.eclipse.jdt.core.dom.TryStatement;
import com.codenvy.eclipse.jdt.core.dom.Type;
import com.codenvy.eclipse.jdt.core.dom.TypeDeclaration;
import com.codenvy.eclipse.jdt.core.dom.TypeDeclarationStatement;
import com.codenvy.eclipse.jdt.core.dom.TypeLiteral;
import com.codenvy.eclipse.jdt.core.dom.TypeParameter;
import com.codenvy.eclipse.jdt.core.dom.UnionType;
import com.codenvy.eclipse.jdt.core.dom.VariableDeclaration;
import com.codenvy.eclipse.jdt.core.dom.VariableDeclarationExpression;
import com.codenvy.eclipse.jdt.core.dom.VariableDeclarationFragment;
import com.codenvy.eclipse.jdt.core.dom.VariableDeclarationStatement;
import com.codenvy.eclipse.jdt.core.dom.WhileStatement;
import com.codenvy.eclipse.jdt.core.dom.WildcardType;

/**
 * <p>
 * This class provides a convenient behaviour-only extension mechanism for the ASTNode hierarchy. If
 * you feel like you would like to add a method to the ASTNode hierarchy (or a subtree of the
 * hierarchy), and you want to have different implementations of it at different points in the
 * hierarchy, simply create a HierarchicalASTVisitor representing the new method and all its
 * implementations, locating each implementation within the right visit(XX) method. If you wanted to
 * add a method implementation to abstract class Foo, an ASTNode descendant, put your implementation
 * in visit(Foo). This class will provide appropriate dispatch, just as if the method
 * implementations had been added to the ASTNode hierarchy.
 * </p>
 *
 * <p>
 * <b>Details:<b>
 * </p>
 *
 * <p>
 * This class has a visit(XX node) method for every class (concrete or abstract) XX in the ASTNode
 * hierarchy. In this class' default implementations of these methods, the method corresponding to a
 * given ASTNode descendant class will call (and return the return value of) the visit(YY) method
 * for it's superclass YY, with the exception of the visit(ASTNode) method which simply returns
 * true, since ASTNode doesn't have a superclass that is within the ASTNode hierarchy.
 * </p>
 *
 * <p>
 * Because of this organization, when visit(XX) methods are overridden in a subclass, and the
 * visitor is applied to a node, only the most specialized overridden method implementation for the
 * node's type will be called, unless this most specialized method calls other visit methods (this
 * is discouraged) or, (preferably) calls super.visit(XX node), (the reference type of the parameter
 * must be XX) which will invoke this class' implementation of the method, which will, in turn,
 * invoke the visit(YY) method corresponding to the superclass, YY.
 * </p>
 *
 * <p>
 * Thus, the dispatching behaviour achieved when HierarchicalASTVisitors' visit(XX) methods,
 * corresponding to a particular concrete or abstract ASTNode descendant class, are overridden is
 * exactly analogous to the dispatching behaviour obtained when method implementations are added to
 * the same ASTNode descendant classes.
 * </p>
 */
/*
 * IMPORTANT NOTE:
 *
 * The structure and behaviour of this class is
 * verified reflectively by
 * org.eclipse.jdt.ui.tests.core.HierarchicalASTVisitorTest
 *
 */
public abstract class HierarchicalASTVisitor extends ASTVisitor
{
   //TODO: check callers for handling of comments

   //---- Begin ASTNode Hierarchy -------------------------------------

   /**
    * Visits the given AST node.
    * <p>
    * The default implementation does nothing and return true. Subclasses may reimplement.
    * </p>
    *
    * @param node the node to visit
    * @return <code>true</code> if the children of this node should be visited, and
    *         <code>false</code> if the children of this node should be skipped
    */
   public boolean visit(ASTNode node)
   {
      return true;
   }

   /**
    * End of visit the given AST node.
    * <p>
    * The default implementation does nothing. Subclasses may reimplement.
    * </p>
    *
    * @param node the node to visit
    */
   public void endVisit(ASTNode node)
   {
      // do nothing
   }

   @Override
   public boolean visit(AnonymousClassDeclaration node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(AnonymousClassDeclaration node)
   {
      endVisit((ASTNode)node);
   }

   //---- Begin BodyDeclaration Hierarchy ---------------------------
   public boolean visit(BodyDeclaration node)
   {
      return visit((ASTNode)node);
   }

   public void endVisit(BodyDeclaration node)
   {
      endVisit((ASTNode)node);
   }

   //---- Begin AbstractTypeDeclaration Hierarchy ---------------------------
   public boolean visit(AbstractTypeDeclaration node)
   {
      return visit((BodyDeclaration)node);
   }

   public void endVisit(AbstractTypeDeclaration node)
   {
      endVisit((BodyDeclaration)node);
   }

   @Override
   public boolean visit(AnnotationTypeDeclaration node)
   {
      return visit((AbstractTypeDeclaration)node);
   }

   @Override
   public void endVisit(AnnotationTypeDeclaration node)
   {
      endVisit((AbstractTypeDeclaration)node);
   }

   @Override
   public boolean visit(EnumDeclaration node)
   {
      return visit((AbstractTypeDeclaration)node);
   }

   @Override
   public void endVisit(EnumDeclaration node)
   {
      endVisit((AbstractTypeDeclaration)node);
   }

   @Override
   public boolean visit(TypeDeclaration node)
   {
      return visit((AbstractTypeDeclaration)node);
   }

   @Override
   public void endVisit(TypeDeclaration node)
   {
      endVisit((AbstractTypeDeclaration)node);
   }

   //---- End AbstractTypeDeclaration Hierarchy ---------------------------

   @Override
   public boolean visit(AnnotationTypeMemberDeclaration node)
   {
      return visit((BodyDeclaration)node);
   }

   @Override
   public void endVisit(AnnotationTypeMemberDeclaration node)
   {
      endVisit((BodyDeclaration)node);
   }

   @Override
   public boolean visit(EnumConstantDeclaration node)
   {
      return visit((BodyDeclaration)node);
   }

   @Override
   public void endVisit(EnumConstantDeclaration node)
   {
      endVisit((BodyDeclaration)node);
   }

   @Override
   public boolean visit(FieldDeclaration node)
   {
      return visit((BodyDeclaration)node);
   }

   @Override
   public void endVisit(FieldDeclaration node)
   {
      endVisit((BodyDeclaration)node);
   }

   @Override
   public boolean visit(Initializer node)
   {
      return visit((BodyDeclaration)node);
   }

   @Override
   public void endVisit(Initializer node)
   {
      endVisit((BodyDeclaration)node);
   }

   @Override
   public boolean visit(MethodDeclaration node)
   {
      return visit((BodyDeclaration)node);
   }

   @Override
   public void endVisit(MethodDeclaration node)
   {
      endVisit((BodyDeclaration)node);
   }

   //---- End BodyDeclaration Hierarchy -----------------------------

   @Override
   public boolean visit(CatchClause node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(CatchClause node)
   {
      endVisit((ASTNode)node);
   }

   //---- Begin Comment Hierarchy ----------------------------------
   public boolean visit(Comment node)
   {
      return visit((ASTNode)node);
   }

   public void endVisit(Comment node)
   {
      endVisit((ASTNode)node);
   }

   @Override
   public boolean visit(BlockComment node)
   {
      return visit((Comment)node);
   }

   @Override
   public void endVisit(BlockComment node)
   {
      endVisit((Comment)node);
   }

   @Override
   public boolean visit(Javadoc node)
   {
      return visit((Comment)node);
   }

   @Override
   public void endVisit(Javadoc node)
   {
      endVisit((Comment)node);
   }

   @Override
   public boolean visit(LineComment node)
   {
      return visit((Comment)node);
   }

   @Override
   public void endVisit(LineComment node)
   {
      endVisit((Comment)node);
   }

   //---- End Comment Hierarchy -----------------------------

   @Override
   public boolean visit(CompilationUnit node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(CompilationUnit node)
   {
      endVisit((ASTNode)node);
   }

   //---- Begin Expression Hierarchy ----------------------------------
   public boolean visit(Expression node)
   {
      return visit((ASTNode)node);
   }

   public void endVisit(Expression node)
   {
      endVisit((ASTNode)node);
   }

   //---- Begin Annotation Hierarchy ----------------------------------
   public boolean visit(Annotation node)
   {
      return visit((Expression)node);
   }

   public void endVisit(Annotation node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(MarkerAnnotation node)
   {
      return visit((Annotation)node);
   }

   @Override
   public void endVisit(MarkerAnnotation node)
   {
      endVisit((Annotation)node);
   }

   @Override
   public boolean visit(NormalAnnotation node)
   {
      return visit((Annotation)node);
   }

   @Override
   public void endVisit(NormalAnnotation node)
   {
      endVisit((Annotation)node);
   }

   @Override
   public boolean visit(SingleMemberAnnotation node)
   {
      return visit((Annotation)node);
   }

   @Override
   public void endVisit(SingleMemberAnnotation node)
   {
      endVisit((Annotation)node);
   }

   //---- End Annotation Hierarchy -----------------------------

   @Override
   public boolean visit(ArrayAccess node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(ArrayAccess node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(ArrayCreation node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(ArrayCreation node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(ArrayInitializer node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(ArrayInitializer node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(Assignment node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(Assignment node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(BooleanLiteral node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(BooleanLiteral node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(CastExpression node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(CastExpression node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(CharacterLiteral node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(CharacterLiteral node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(ClassInstanceCreation node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(ClassInstanceCreation node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(ConditionalExpression node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(ConditionalExpression node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(FieldAccess node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(FieldAccess node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(InfixExpression node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(InfixExpression node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(InstanceofExpression node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(InstanceofExpression node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(MethodInvocation node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(MethodInvocation node)
   {
      endVisit((Expression)node);
   }

   //---- Begin Name Hierarchy ----------------------------------
   public boolean visit(Name node)
   {
      return visit((Expression)node);
   }

   public void endVisit(Name node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(QualifiedName node)
   {
      return visit((Name)node);
   }

   @Override
   public void endVisit(QualifiedName node)
   {
      endVisit((Name)node);
   }

   @Override
   public boolean visit(SimpleName node)
   {
      return visit((Name)node);
   }

   @Override
   public void endVisit(SimpleName node)
   {
      endVisit((Name)node);
   }

   //---- End Name Hierarchy ------------------------------------

   @Override
   public boolean visit(NullLiteral node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(NullLiteral node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(NumberLiteral node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(NumberLiteral node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(ParenthesizedExpression node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(ParenthesizedExpression node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(PostfixExpression node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(PostfixExpression node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(PrefixExpression node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(PrefixExpression node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(StringLiteral node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(StringLiteral node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(SuperFieldAccess node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(SuperFieldAccess node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(SuperMethodInvocation node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(SuperMethodInvocation node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(ThisExpression node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(ThisExpression node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(TypeLiteral node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(TypeLiteral node)
   {
      endVisit((Expression)node);
   }

   @Override
   public boolean visit(VariableDeclarationExpression node)
   {
      return visit((Expression)node);
   }

   @Override
   public void endVisit(VariableDeclarationExpression node)
   {
      endVisit((Expression)node);
   }

   //---- End Expression Hierarchy ----------------------------------

   @Override
   public boolean visit(ImportDeclaration node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(ImportDeclaration node)
   {
      endVisit((ASTNode)node);
   }

   @Override
   public boolean visit(MemberRef node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(MemberRef node)
   {
      endVisit((ASTNode)node);
   }

   @Override
   public boolean visit(MemberValuePair node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(MemberValuePair node)
   {
      endVisit((ASTNode)node);
   }

   @Override
   public boolean visit(MethodRef node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(MethodRef node)
   {
      endVisit((ASTNode)node);
   }

   @Override
   public boolean visit(MethodRefParameter node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(MethodRefParameter node)
   {
      endVisit((ASTNode)node);
   }

   @Override
   public boolean visit(Modifier node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(Modifier node)
   {
      endVisit((ASTNode)node);
   }

   @Override
   public boolean visit(PackageDeclaration node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(PackageDeclaration node)
   {
      endVisit((ASTNode)node);
   }

   //---- Begin Statement Hierarchy ---------------------------------
   public boolean visit(Statement node)
   {
      return visit((ASTNode)node);
   }

   public void endVisit(Statement node)
   {
      endVisit((ASTNode)node);
   }


   @Override
   public boolean visit(AssertStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(AssertStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(Block node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(Block node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(BreakStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(BreakStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(ConstructorInvocation node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(ConstructorInvocation node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(ContinueStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(ContinueStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(DoStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(DoStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(EmptyStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(EmptyStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(EnhancedForStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(EnhancedForStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(ExpressionStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(ExpressionStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(ForStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(ForStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(IfStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(IfStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(LabeledStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(LabeledStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(ReturnStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(ReturnStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(SuperConstructorInvocation node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(SuperConstructorInvocation node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(SwitchCase node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(SwitchCase node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(SwitchStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(SwitchStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(SynchronizedStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(SynchronizedStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(ThrowStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(ThrowStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(TryStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(TryStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(TypeDeclarationStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(TypeDeclarationStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(VariableDeclarationStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(VariableDeclarationStatement node)
   {
      endVisit((Statement)node);
   }

   @Override
   public boolean visit(WhileStatement node)
   {
      return visit((Statement)node);
   }

   @Override
   public void endVisit(WhileStatement node)
   {
      endVisit((Statement)node);
   }

   //---- End Statement Hierarchy ----------------------------------

   @Override
   public boolean visit(TagElement node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(TagElement node)
   {
      endVisit((ASTNode)node);
   }

   @Override
   public boolean visit(TextElement node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(TextElement node)
   {
      endVisit((ASTNode)node);
   }


   //---- Begin Type Hierarchy --------------------------------------
   public boolean visit(Type node)
   {
      return visit((ASTNode)node);
   }

   public void endVisit(Type node)
   {
      endVisit((ASTNode)node);
   }

   @Override
   public boolean visit(ArrayType node)
   {
      return visit((Type)node);
   }

   @Override
   public void endVisit(ArrayType node)
   {
      endVisit((Type)node);
   }

   @Override
   public boolean visit(ParameterizedType node)
   {
      return visit((Type)node);
   }

   @Override
   public void endVisit(ParameterizedType node)
   {
      endVisit((Type)node);
   }

   @Override
   public boolean visit(PrimitiveType node)
   {
      return visit((Type)node);
   }

   @Override
   public void endVisit(PrimitiveType node)
   {
      endVisit((Type)node);
   }

   @Override
   public boolean visit(QualifiedType node)
   {
      return visit((Type)node);
   }

   @Override
   public void endVisit(QualifiedType node)
   {
      endVisit((Type)node);
   }

   @Override
   public boolean visit(SimpleType node)
   {
      return visit((Type)node);
   }

   @Override
   public void endVisit(SimpleType node)
   {
      endVisit((Type)node);
   }

   @Override
   public boolean visit(WildcardType node)
   {
      return visit((Type)node);
   }

   @Override
   public void endVisit(WildcardType node)
   {
      endVisit((Type)node);
   }

   @Override
   public boolean visit(UnionType node)
   {
      return visit((Type)node);
   }

   @Override
   public void endVisit(UnionType node)
   {
      endVisit((Type)node);
   }

   //---- End Type Hierarchy ----------------------------------------

   @Override
   public boolean visit(TypeParameter node)
   {
      return visit((ASTNode)node);
   }

   @Override
   public void endVisit(TypeParameter node)
   {
      endVisit((ASTNode)node);
   }


   //---- Begin VariableDeclaration Hierarchy ---------------------------
   public boolean visit(VariableDeclaration node)
   {
      return visit((ASTNode)node);
   }

   public void endVisit(VariableDeclaration node)
   {
      endVisit((ASTNode)node);
   }

   @Override
   public boolean visit(SingleVariableDeclaration node)
   {
      return visit((VariableDeclaration)node);
   }

   @Override
   public void endVisit(SingleVariableDeclaration node)
   {
      endVisit((VariableDeclaration)node);
   }

   @Override
   public boolean visit(VariableDeclarationFragment node)
   {
      return visit((VariableDeclaration)node);
   }

   @Override
   public void endVisit(VariableDeclarationFragment node)
   {
      endVisit((VariableDeclaration)node);
   }

   //---- End VariableDeclaration Hierarchy -----------------------------
   //---- End ASTNode Hierarchy -----------------------------------------
}
