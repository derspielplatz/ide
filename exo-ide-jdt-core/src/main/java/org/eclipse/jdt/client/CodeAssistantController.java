/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.eclipse.jdt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;

import org.eclipse.jdt.client.codeassistant.AbstractJavaCompletionProposal;
import org.eclipse.jdt.client.codeassistant.CompletionProposalCollector;
import org.eclipse.jdt.client.codeassistant.FillArgumentNamesCompletionProposalCollector;
import org.eclipse.jdt.client.codeassistant.api.IJavaCompletionProposal;
import org.eclipse.jdt.client.codeassistant.ui.CodeAssitantForm;
import org.eclipse.jdt.client.codeassistant.ui.ProposalSelectedHandler;
import org.eclipse.jdt.client.codeassistant.ui.ProposalWidget;
import org.eclipse.jdt.client.compiler.batch.CompilationUnit;
import org.eclipse.jdt.client.core.CompletionProposal;
import org.eclipse.jdt.client.core.JavaCore;
import org.eclipse.jdt.client.core.dom.AST;
import org.eclipse.jdt.client.core.dom.ASTNode;
import org.eclipse.jdt.client.core.dom.ASTParser;
import org.eclipse.jdt.client.event.CancelParseEvent;
import org.eclipse.jdt.client.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.client.runtime.IProgressMonitor;
import org.eclipse.jdt.client.text.Document;
import org.eclipse.jdt.client.text.IDocument;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage.Type;
import org.exoplatform.ide.editor.api.codeassitant.RunCodeAssistantEvent;
import org.exoplatform.ide.editor.api.codeassitant.RunCodeAssistantHandler;
import org.exoplatform.ide.editor.codemirror.CodeMirror;
import org.exoplatform.ide.vfs.client.model.FileModel;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version ${Id}: Jan 24, 2012 5:11:46 PM evgen $
 */
public class CodeAssistantController implements RunCodeAssistantHandler, EditorActiveFileChangedHandler,
   ProposalSelectedHandler
{

   /**
    * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
    * @version $Id: 2:09:49 PM 34360 2009-07-22 23:58:59Z evgen $
    * 
    */
   private static final class ProgressMonitor implements IProgressMonitor
   {
      private final static int TIMEOUT = 60000; // ms

      private long endTime;

      public void beginTask(String name, int totalWork)
      {
         endTime = System.currentTimeMillis() + TIMEOUT;
      }

      public boolean isCanceled()
      {
         return false;// endTime <= System.currentTimeMillis();
      }

      @Override
      public void done()
      {
      }

      @Override
      public void internalWorked(double work)
      {
      }

      @Override
      public void setCanceled(boolean value)
      {
      }

      @Override
      public void setTaskName(String name)
      {
      }

      @Override
      public void subTask(String name)
      {
      }

      @Override
      public void worked(int work)
      {
      }
   }

   private FileModel currentFile;

   private CodeMirror currentEditor;

   private String afterToken;

   private String tokenToComplete;

   private String beforeToken;

   private int currentLineNumber;
   
//   private TemplateCompletionProposalComputer templateCompletionProposalComputer = new TemplateCompletionProposalComputer();

   /**
    * 
    */
   public CodeAssistantController()
   {
      IDE.addHandler(RunCodeAssistantEvent.TYPE, this);
      IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);
   }

   /** @see org.exoplatform.ide.editor.api.codeassitant.RunCodeAssistantHandler#onRunCodeAssistant(org.exoplatform.ide.editor.api.codeassitant.RunCodeAssistantEvent) */
   @Override
   public void onRunCodeAssistant(RunCodeAssistantEvent event)
   {
      if (currentFile == null || currentEditor == null)
         return;
      IDE.fireEvent(new CancelParseEvent());
      GWT.runAsync(new RunAsyncCallback()
      {

         @Override
         public void onSuccess()
         {
            codecomplete();
         }

         @Override
         public void onFailure(Throwable reason)
         {
            // TODO Auto-generated method stub
            reason.printStackTrace();
         }
      });
   }

   /**
    * 
    */
   private void codecomplete()
   {
      ASTParser parser = ASTParser.newParser(AST.JLS3);
      IDocument document = new Document(currentEditor.getText());
      parser.setSource(document.get().toCharArray());
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setUnitName(currentFile.getName().substring(0, currentFile.getName().lastIndexOf('.')));
      parser.setNameEnvironment(new NameEnvironment(currentFile.getProject().getId()));
      parser.setResolveBindings(true);
      ASTNode ast = parser.createAST(null);
      org.eclipse.jdt.client.core.dom.CompilationUnit unit = (org.eclipse.jdt.client.core.dom.CompilationUnit)ast;

      int completionPosition = unit.getPosition(currentEditor.getCursorRow(), currentEditor.getCursorCol() -1);
//         getCompletionPosition(currentFile.getContent(), currentEditor.getCursorRow(), currentEditor.getCursorCol());
      CompletionProposalCollector collector =
         new FillArgumentNamesCompletionProposalCollector(unit, document, completionPosition, currentFile.getProject()
            .getId(), JdtExtension.DOC_CONTEXT);
      collector
         .setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF, true);
      collector.setRequireExtendedContext(true);
      char[] fileContent = document.get().toCharArray();
      CompletionEngine e =
         new CompletionEngine(new NameEnvironment(currentFile.getProject().getId()), collector,
            JavaCore.getOptions(), new ProgressMonitor());

      try
      {
         e.complete(
            new CompilationUnit(fileContent,
               currentFile.getName().substring(0, currentFile.getName().lastIndexOf('.')), "UTF-8"),
            completionPosition, 0);

         currentLineNumber = currentEditor.getCursorRow();
         String lineContent = currentEditor.getLineContent(currentLineNumber);

         parseTokenLine(lineContent, currentEditor.getCursorCol());

         int posX = currentEditor.getCursorOffsetX() - tokenToComplete.length() * 8 + 8;
         int posY = currentEditor.getCursorOffsetY() + 4;
         IJavaCompletionProposal[] javaCompletionProposals = collector.getJavaCompletionProposals();
         
//         List<IJavaCompletionProposal> templateProposals = templateCompletionProposalComputer.computeCompletionProposals(collector.getInvocationContext(), null);
//         IJavaCompletionProposal[] array = templateProposals.toArray(new IJavaCompletionProposal[templateProposals.size()]);
//         IJavaCompletionProposal[] proposals =
//            new IJavaCompletionProposal[javaCompletionProposals.length + array.length];
//         System.arraycopy(javaCompletionProposals, 0, proposals, 0, javaCompletionProposals.length);
//         System.arraycopy(array, 0, proposals, javaCompletionProposals.length, array.length);
         
         Arrays.sort(javaCompletionProposals, comparator);
         new CodeAssitantForm(posX, posY, tokenToComplete, javaCompletionProposals, this);
      }
      catch (Exception ex)
      {
         String st = ex.getClass().getName() + ": " + ex.getMessage();
         for (StackTraceElement ste : ex.getStackTrace())
            st += "\n" + ste.toString();
         IDE.fireEvent(new OutputEvent(st, Type.ERROR));
      }
   }

   /**
    * @param line
    */
   private void parseTokenLine(String line, int cursorPos)
   {
      String tokenLine = "";
      tokenToComplete = "";
      afterToken = "";
      beforeToken = "";
      if (line.length() > cursorPos - 1)
      {
         afterToken = line.substring(cursorPos - 1, line.length());
         tokenLine = line.substring(0, cursorPos - 1);

      }
      else
      {
         afterToken = "";
         if (line.endsWith(" "))
         {
            tokenToComplete = "";
            beforeToken = line;
            return;
         }

         tokenLine = line;
      }

      for (int i = tokenLine.length() - 1; i >= 0; i--)
      {
         switch (tokenLine.charAt(i))
         {
            case ' ' :
            case '.' :
            case '(' :
            case ')' :
            case '{' :
            case '}' :
            case ';' :
            case '[' :
            case ']' :
            case '+' :
            case '=' :
            case '-' :
            case '|' :
            case '&' :
            case ':' :
            case '*' :
            case '/' :
            case '?' :
            case '"' :
            case '\'' :
            case '<' :
            case '>' :
            case ',' :
            case '@' :
               beforeToken = tokenLine.substring(0, i + 1);
               tokenToComplete = tokenLine.substring(i + 1);
               return;

            default :
               break;
         }
         beforeToken = "";
         tokenToComplete = tokenLine;
      }

   }

   private Comparator<IJavaCompletionProposal> comparator = new Comparator<IJavaCompletionProposal>()
   {

      @Override
      public int compare(IJavaCompletionProposal o1, IJavaCompletionProposal o2)
      {

         if (o1.getRelevance() > o2.getRelevance())
            return -1;
         else if (o1.getRelevance() < o2.getRelevance())
            return 1;
         else
            return 0;
      }
   };

   /** @see org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler#onEditorActiveFileChanged(org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent) */
   @Override
   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      currentFile = event.getFile();
      if (event.getEditor() instanceof CodeMirror)
         currentEditor = (CodeMirror)event.getEditor();
      else
         currentEditor = null;
   }

   /** @see org.eclipse.jdt.client.codeassistant.ui.ProposalSelectedHandler#onStringSelected(java.lang.String) */
   @Override
   public void onStringSelected(String value)
   {
      currentEditor.replaceTextAtCurrentLine(beforeToken + value + afterToken, (beforeToken + value).length());
   }

   /** @see org.eclipse.jdt.client.codeassistant.ui.ProposalSelectedHandler#onTokenSelected(org.eclipse.jdt.client.codeassistant.ui.ProposalWidget) */
   @Override
   public void onTokenSelected(ProposalWidget value)
   {
      try
      {
         IJavaCompletionProposal proposal = value.getProposal();
         IDocument document = new Document(currentEditor.getText());
         proposal.apply(document);
         currentEditor.setText(document.get());
         if (proposal instanceof AbstractJavaCompletionProposal)
         {
            AbstractJavaCompletionProposal proposal2 = (AbstractJavaCompletionProposal)proposal;
            int cursorPosition = proposal2.getCursorPosition();
            int replacementOffset = proposal2.getReplacementOffset();
            String string = document.get(0, replacementOffset + cursorPosition);
            String[] split = string.split("\n");
            currentEditor.goToPosition(split.length, split[split.length - 1].length() + 1);
            currentEditor.setFocus();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         IDE.fireEvent(new OutputEvent(e.getMessage(), Type.ERROR));
      }
   }

   /** @see org.eclipse.jdt.client.codeassistant.ui.ProposalSelectedHandler#onCancelAutoComplete() */
   @Override
   public void onCancelAutoComplete()
   {
      currentEditor.setFocus();
   }

}
