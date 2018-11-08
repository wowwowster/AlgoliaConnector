package com.sword.gsa.spis.scs.extracting.pdfbox.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.Bidi;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDThreadBead;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.text.TextPositionComparator;
import org.apache.pdfbox.util.QuickSort;

public class SCSPDFTextStripper extends SCSLegacyPDFStreamEngine {
    private static float defaultIndentThreshold = 2.0F;
    private static float defaultDropThreshold = 2.5F;
    private static final boolean useCustomQuickSort;
    private static final Log LOG = LogFactory.getLog(SCSPDFTextStripper.class);
    protected final String LINE_SEPARATOR = System.getProperty("line.separator");
    private String lineSeparator;
    private String wordSeparator;
    private String paragraphStart;
    private String paragraphEnd;
    private String pageStart;
    private String pageEnd;
    private String articleStart;
    private String articleEnd;
    private int currentPageNo;
    private int startPage;
    private int endPage;
    private PDOutlineItem startBookmark;
    private int startBookmarkPageNumber;
    private int endBookmarkPageNumber;
    private PDOutlineItem endBookmark;
    private boolean suppressDuplicateOverlappingText;
    private boolean shouldSeparateByBeads;
    private boolean sortByPosition;
    private boolean addMoreFormatting;
    private float indentThreshold;
    private float dropThreshold;
    private float spacingTolerance;
    private float averageCharTolerance;
    private List<PDRectangle> beadRectangles;
    protected ArrayList<List<TextPosition>> charactersByArticle;
    private Map<String, TreeMap<Float, TreeSet<Float>>> characterListMapping;
    protected PDDocument document;
    protected Writer output;
    private boolean inParagraph;
    private static final float END_OF_LAST_TEXT_X_RESET_VALUE = -1.0F;
    private static final float MAX_Y_FOR_LINE_RESET_VALUE = -3.4028235E38F;
    private static final float EXPECTED_START_OF_NEXT_WORD_X_RESET_VALUE = -3.4028235E38F;
    private static final float MAX_HEIGHT_FOR_LINE_RESET_VALUE = -1.0F;
    private static final float MIN_Y_TOP_FOR_LINE_RESET_VALUE = 3.4028235E38F;
    private static final float LAST_WORD_SPACING_RESET_VALUE = -1.0F;
    private static final String[] LIST_ITEM_EXPRESSIONS;
    private List<Pattern> listOfPatterns;
    private static Map<Character, Character> MIRRORING_CHAR_MAP;
    private PDFont prevPDFont;

    public SCSPDFTextStripper() throws IOException {
        this.lineSeparator = this.LINE_SEPARATOR;
        this.wordSeparator = " ";
        this.paragraphStart = "";
        this.paragraphEnd = "";
        this.pageStart = "";
        this.pageEnd = this.LINE_SEPARATOR;
        this.articleStart = "";
        this.articleEnd = "";
        this.currentPageNo = 0;
        this.startPage = 1;
        this.endPage = 2147483647;
        this.startBookmark = null;
        this.startBookmarkPageNumber = -1;
        this.endBookmarkPageNumber = -1;
        this.endBookmark = null;
        this.suppressDuplicateOverlappingText = true;
        this.shouldSeparateByBeads = true;
        this.sortByPosition = true;
        this.addMoreFormatting = false;
        this.indentThreshold = defaultIndentThreshold;
        this.dropThreshold = defaultDropThreshold;
        this.spacingTolerance = 0.5F;
        this.averageCharTolerance = 0.3F;
        this.beadRectangles = null;
        this.charactersByArticle = new ArrayList();
        this.characterListMapping = new HashMap();
        this.listOfPatterns = null;
    }

    public String getText(PDDocument doc) throws IOException {
        StringWriter outputStream = new StringWriter();
        this.writeText(doc, outputStream);
        return outputStream.toString();
    }

    private void resetEngine() {
        this.currentPageNo = 0;
        this.document = null;
        if (this.charactersByArticle != null) {
            this.charactersByArticle.clear();
        }

        if (this.characterListMapping != null) {
            this.characterListMapping.clear();
        }

    }

    public void writeText(PDDocument doc, Writer outputStream) throws IOException {
        this.resetEngine();
        this.document = doc;
        this.output = outputStream;
        if (this.getAddMoreFormatting()) {
            this.paragraphEnd = this.lineSeparator;
            this.pageStart = this.lineSeparator;
            this.articleStart = this.lineSeparator;
            this.articleEnd = this.lineSeparator;
        }

        this.startDocument(this.document);
        this.processPages(this.document.getPages());
        this.endDocument(this.document);
    }

    protected void processPages(PDPageTree pages) throws IOException {
        PDPage startBookmarkPage = this.startBookmark == null ? null : this.startBookmark.findDestinationPage(this.document);
        if (startBookmarkPage != null) {
            this.startBookmarkPageNumber = pages.indexOf(startBookmarkPage) + 1;
        } else {
            this.startBookmarkPageNumber = -1;
        }

        PDPage endBookmarkPage = this.endBookmark == null ? null : this.endBookmark.findDestinationPage(this.document);
        if (endBookmarkPage != null) {
            this.endBookmarkPageNumber = pages.indexOf(endBookmarkPage) + 1;
        } else {
            this.endBookmarkPageNumber = -1;
        }

        if (this.startBookmarkPageNumber == -1 && this.startBookmark != null && this.endBookmarkPageNumber == -1 && this.endBookmark != null && this.startBookmark.getCOSObject() == this.endBookmark.getCOSObject()) {
            this.startBookmarkPageNumber = 0;
            this.endBookmarkPageNumber = 0;
        }

        Iterator var4 = pages.iterator();

        while (var4.hasNext()) {
            PDPage page = (PDPage) var4.next();
            ++this.currentPageNo;
            if (page.hasContents()) {
                this.processPage(page);
            }
        }

    }

    protected void startDocument(PDDocument document) throws IOException {
    }

    protected void endDocument(PDDocument document) throws IOException {
    }

    public void processPage(PDPage page) throws IOException {
        if (this.currentPageNo >= this.startPage && this.currentPageNo <= this.endPage && (this.startBookmarkPageNumber == -1 || this.currentPageNo >= this.startBookmarkPageNumber) && (this.endBookmarkPageNumber == -1 || this.currentPageNo <= this.endBookmarkPageNumber)) {
            this.startPage(page);
            int numberOfArticleSections = 1;
            if (this.shouldSeparateByBeads) {
                this.fillBeadRectangles(page);
                numberOfArticleSections += this.beadRectangles.size() * 2;
            }

            int originalSize = this.charactersByArticle.size();
            this.charactersByArticle.ensureCapacity(numberOfArticleSections);
            int lastIndex = Math.max(numberOfArticleSections, originalSize);

            for (int i = 0; i < lastIndex; ++i) {
                if (i < originalSize) {
                    ((List) this.charactersByArticle.get(i)).clear();
                } else if (numberOfArticleSections < originalSize) {
                    this.charactersByArticle.remove(i);
                } else {
                    this.charactersByArticle.add(new ArrayList());
                }
            }

            this.characterListMapping.clear();
            super.processPage(page);
            this.writePage();
            this.endPage(page);
        }

    }

    private void fillBeadRectangles(PDPage page) {
        this.beadRectangles = new ArrayList();
        Iterator var2 = page.getThreadBeads().iterator();

        while (true) {
            while (var2.hasNext()) {
                PDThreadBead bead = (PDThreadBead) var2.next();
                if (bead == null) {
                    this.beadRectangles.add(null);
                } else {
                    PDRectangle rect = bead.getRectangle();
                    PDRectangle mediaBox = page.getMediaBox();
                    float upperRightY = mediaBox.getUpperRightY() - rect.getLowerLeftY();
                    float lowerLeftY = mediaBox.getUpperRightY() - rect.getUpperRightY();
                    rect.setLowerLeftY(lowerLeftY);
                    rect.setUpperRightY(upperRightY);
                    PDRectangle cropBox = page.getCropBox();
                    if (cropBox.getLowerLeftX() != 0.0F || cropBox.getLowerLeftY() != 0.0F) {
                        rect.setLowerLeftX(rect.getLowerLeftX() - cropBox.getLowerLeftX());
                        rect.setLowerLeftY(rect.getLowerLeftY() - cropBox.getLowerLeftY());
                        rect.setUpperRightX(rect.getUpperRightX() - cropBox.getLowerLeftX());
                        rect.setUpperRightY(rect.getUpperRightY() - cropBox.getLowerLeftY());
                    }

                    this.beadRectangles.add(rect);
                }
            }

            return;
        }
    }

    protected void startArticle() throws IOException {
        this.startArticle(true);
    }

    protected void startArticle(boolean isLTR) throws IOException {
        this.output.write(this.getArticleStart());
    }

    protected void endArticle() throws IOException {
        this.output.write(this.getArticleEnd());
    }

    protected void startPage(PDPage page) throws IOException {
    }

    protected void endPage(PDPage page) throws IOException {
    }

    protected void writePage() throws IOException {
        float maxYForLine = -3.4028235E38F;
        float minYTopForLine = 3.4028235E38F;
        float endOfLastTextX = -1.0F;
        float lastWordSpacing = -1.0F;
        float maxHeightForLine = -1.0F;
        SCSPDFTextStripper.PositionWrapper lastPosition = null;
        SCSPDFTextStripper.PositionWrapper lastPositionNotSpace = null;
        SCSPDFTextStripper.PositionWrapper lastLineStartPosition = null;
        boolean startOfPage = true;
        if (this.charactersByArticle.size() > 0) {
            this.writePageStart();
        }

        for (Iterator var10 = this.charactersByArticle.iterator(); var10.hasNext(); this.endArticle()) {
            List<TextPosition> textList = (List) var10.next();
            if (this.getSortByPosition()) {
                TextPositionComparator comparator = new TextPositionComparator();
                if (useCustomQuickSort) {
                    QuickSort.sort(textList, comparator);
                } else {
                    Collections.sort(textList, comparator);
                }
            }

            this.startArticle();
            boolean startOfArticle = true;
            List<SCSPDFTextStripper.LineItem> line = new ArrayList();
            Iterator<TextPosition> textIter = textList.iterator();

            float averageCharWidth;
            for (float previousAveCharWidth = -1.0F; textIter.hasNext(); previousAveCharWidth = averageCharWidth) {
                TextPosition position = (TextPosition) textIter.next();
                SCSPDFTextStripper.PositionWrapper current = new SCSPDFTextStripper.PositionWrapper(position);
                String characterValue = position.getUnicode();
                if (lastPosition != null && (position.getFont() != lastPosition.getTextPosition().getFont() || position.getFontSize() != lastPosition.getTextPosition().getFontSize())) {
                    previousAveCharWidth = -1.0F;
                }

                float positionX;
                float positionY;
                float positionWidth;
                float positionHeight;
                if (this.getSortByPosition()) {
                    positionX = position.getXDirAdj();
                    positionY = position.getYDirAdj();
                    positionWidth = position.getWidthDirAdj();
                    positionHeight = position.getHeightDir();
                } else {
                    positionX = position.getX();
                    positionY = position.getY();
                    positionWidth = position.getWidth();
                    positionHeight = position.getHeight();
                }

                int wordCharCount = position.getIndividualWidths().length;
                float wordSpacing = position.getWidthOfSpace();
                float deltaSpace;
                if (wordSpacing != 0.0F && !Float.isNaN(wordSpacing)) {
                    if (lastWordSpacing < 0.0F) {
                        deltaSpace = wordSpacing * this.getSpacingTolerance();
                    } else {
                        deltaSpace = (wordSpacing + lastWordSpacing) / 2.0F * this.getSpacingTolerance();
                    }
                } else {
                    deltaSpace = 3.4028235E38F;
                }

                if (previousAveCharWidth < 0.0F) {
                    averageCharWidth = positionWidth / (float) wordCharCount;
                } else {
                    averageCharWidth = (previousAveCharWidth + positionWidth / (float) wordCharCount) / 2.0F;
                }

                float deltaCharWidth = averageCharWidth * this.getAverageCharTolerance();
                float expectedStartOfNextWordX = -3.4028235E38F;
                if (endOfLastTextX != -1.0F) {
                    if (deltaCharWidth > deltaSpace) {
                        expectedStartOfNextWordX = endOfLastTextX + deltaSpace;
                    } else {
                        expectedStartOfNextWordX = endOfLastTextX + deltaCharWidth;
                    }
                }

                if (lastPosition != null) {
                    if (startOfArticle) {
                        lastPosition.setArticleStart();
                        startOfArticle = false;
                    }

                    if (!this.overlap(positionY, positionHeight, maxYForLine, maxHeightForLine)) {
                        this.writeLine(this.normalize(line));
                        line.clear();
                        lastLineStartPosition = this.handleLineSeparation(current, lastPosition, lastLineStartPosition, maxHeightForLine, lastPositionNotSpace);
                        expectedStartOfNextWordX = -3.4028235E38F;
                        maxYForLine = -3.4028235E38F;
                        maxHeightForLine = -1.0F;
                        minYTopForLine = 3.4028235E38F;
                    }

                    if (expectedStartOfNextWordX != -3.4028235E38F && expectedStartOfNextWordX < positionX && lastPosition.getTextPosition().getUnicode() != null && !lastPosition.getTextPosition().getUnicode().endsWith(" ")) {
                        line.add(SCSPDFTextStripper.LineItem.WORD_SEPARATOR);
                    }
                }

                if (positionY >= maxYForLine) {
                    maxYForLine = positionY;
                }

                endOfLastTextX = positionX + positionWidth;
                if (characterValue != null) {
                    if (startOfPage && lastPosition == null) {
                        this.writeParagraphStart();
                    }

                    line.add(new SCSPDFTextStripper.LineItem(position));
                }

                maxHeightForLine = Math.max(maxHeightForLine, positionHeight);
                minYTopForLine = Math.min(minYTopForLine, positionY - positionHeight);
                lastPosition = current;
                if (current != null && current.getTextPosition()!= null && current.getTextPosition().getUnicode() != null && !current.getTextPosition().getUnicode().equalsIgnoreCase(" ")) {
                    lastPositionNotSpace = current;
                }
                if (startOfPage) {
                    current.setParagraphStart();
                    current.setLineStart();
                    lastLineStartPosition = current;
                    startOfPage = false;
                }

                lastWordSpacing = wordSpacing;
            }

            if (line.size() > 0) {
                this.writeLine(this.normalize(line));
                this.writeParagraphEnd();
            }
        }

        this.writePageEnd();
    }

    private boolean overlap(float y1, float height1, float y2, float height2) {
        return this.within(y1, y2, 0.1F) || y2 <= y1 && y2 >= y1 - height1 || y1 <= y2 && y1 >= y2 - height2;
    }

    protected void writeLineSeparator() throws IOException {
        this.output.write(this.getLineSeparator());
    }

    protected void writeWordSeparator() throws IOException {
        this.output.write(this.getWordSeparator());
    }

    protected void writeCharacters(TextPosition text) throws IOException {
        this.output.write(text.getUnicode());
    }

    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        this.writeString(text);
    }

    protected void writeString(String text) throws IOException {
        this.output.write(text);
    }

    private boolean within(float first, float second, float variance) {
        return second < first + variance && second > first - variance;
    }

    protected void processTextPosition(TextPosition text) {
        boolean showCharacter = true;
        float y;
        if (this.suppressDuplicateOverlappingText) {
            showCharacter = false;
            String textCharacter = text.getUnicode();
            float textX = text.getX();
            float textY = text.getY();
            TreeMap<Float, TreeSet<Float>> sameTextCharacters = (TreeMap) this.characterListMapping.get(textCharacter);
            if (sameTextCharacters == null) {
                sameTextCharacters = new TreeMap();
                this.characterListMapping.put(textCharacter, sameTextCharacters);
            }

            boolean suppressCharacter = false;
            y = text.getWidth() / (float) textCharacter.length() / 3.0F;
            SortedMap<Float, TreeSet<Float>> xMatches = sameTextCharacters.subMap(Float.valueOf(textX - y), Float.valueOf(textX + y));
            Iterator var10 = xMatches.values().iterator();

            while (var10.hasNext()) {
                TreeSet<Float> xMatch = (TreeSet) var10.next();
                SortedSet<Float> yMatches = xMatch.subSet(Float.valueOf(textY - y), Float.valueOf(textY + y));
                if (!yMatches.isEmpty()) {
                    suppressCharacter = true;
                    break;
                }
            }

            if (!suppressCharacter) {
                TreeSet<Float> ySet = (TreeSet) sameTextCharacters.get(Float.valueOf(textX));
                if (ySet == null) {
                    ySet = new TreeSet();
                    sameTextCharacters.put(Float.valueOf(textX), ySet);
                }

                ySet.add(Float.valueOf(textY));
                showCharacter = true;
            }
        }

        if (showCharacter) {
            int foundArticleDivisionIndex = -1;
            int notFoundButFirstLeftAndAboveArticleDivisionIndex = -1;
            int notFoundButFirstLeftArticleDivisionIndex = -1;
            int notFoundButFirstAboveArticleDivisionIndex = -1;
            float x = text.getX();
            y = text.getY();
            int i;
            if (!this.shouldSeparateByBeads) {
                foundArticleDivisionIndex = 0;
            } else {
                for (i = 0; i < this.beadRectangles.size() && foundArticleDivisionIndex == -1; ++i) {
                    PDRectangle rect = (PDRectangle) this.beadRectangles.get(i);
                    if (rect != null) {
                        if (rect.contains(x, y)) {
                            foundArticleDivisionIndex = i * 2 + 1;
                        } else if ((x < rect.getLowerLeftX() || y < rect.getUpperRightY()) && notFoundButFirstLeftAndAboveArticleDivisionIndex == -1) {
                            notFoundButFirstLeftAndAboveArticleDivisionIndex = i * 2;
                        } else if (x < rect.getLowerLeftX() && notFoundButFirstLeftArticleDivisionIndex == -1) {
                            notFoundButFirstLeftArticleDivisionIndex = i * 2;
                        } else if (y < rect.getUpperRightY() && notFoundButFirstAboveArticleDivisionIndex == -1) {
                            notFoundButFirstAboveArticleDivisionIndex = i * 2;
                        }
                    } else {
                        foundArticleDivisionIndex = 0;
                    }
                }
            }

            if (foundArticleDivisionIndex != -1) {
                i = foundArticleDivisionIndex;
            } else if (notFoundButFirstLeftAndAboveArticleDivisionIndex != -1) {
                i = notFoundButFirstLeftAndAboveArticleDivisionIndex;
            } else if (notFoundButFirstLeftArticleDivisionIndex != -1) {
                i = notFoundButFirstLeftArticleDivisionIndex;
            } else if (notFoundButFirstAboveArticleDivisionIndex != -1) {
                i = notFoundButFirstAboveArticleDivisionIndex;
            } else {
                i = this.charactersByArticle.size() - 1;
            }

            List<TextPosition> textList = (List) this.charactersByArticle.get(i);
            if (textList.isEmpty()) {
                textList.add(text);
            } else {
                TextPosition previousTextPosition = (TextPosition) textList.get(textList.size() - 1);
                if (text.isDiacritic() && previousTextPosition.contains(text)) {
                    previousTextPosition.mergeDiacritic(text);
                } else if (previousTextPosition.isDiacritic() && text.contains(previousTextPosition)) {
                    text.mergeDiacritic(previousTextPosition);
                    textList.remove(textList.size() - 1);
                    textList.add(text);
                } else {
                    textList.add(text);
                }
            }
        }

    }

    public int getStartPage() {
        return this.startPage;
    }

    public void setStartPage(int startPageValue) {
        this.startPage = startPageValue;
    }

    public int getEndPage() {
        return this.endPage;
    }

    public void setEndPage(int endPageValue) {
        this.endPage = endPageValue;
    }

    public void setLineSeparator(String separator) {
        this.lineSeparator = separator;
    }

    public String getLineSeparator() {
        return this.lineSeparator;
    }

    public String getWordSeparator() {
        return this.wordSeparator;
    }

    public void setWordSeparator(String separator) {
        this.wordSeparator = separator;
    }

    public boolean getSuppressDuplicateOverlappingText() {
        return this.suppressDuplicateOverlappingText;
    }

    protected int getCurrentPageNo() {
        return this.currentPageNo;
    }

    protected Writer getOutput() {
        return this.output;
    }

    protected List<List<TextPosition>> getCharactersByArticle() {
        return this.charactersByArticle;
    }

    public void setSuppressDuplicateOverlappingText(boolean suppressDuplicateOverlappingTextValue) {
        this.suppressDuplicateOverlappingText = suppressDuplicateOverlappingTextValue;
    }

    public boolean getSeparateByBeads() {
        return this.shouldSeparateByBeads;
    }

    public void setShouldSeparateByBeads(boolean aShouldSeparateByBeads) {
        this.shouldSeparateByBeads = aShouldSeparateByBeads;
    }

    public PDOutlineItem getEndBookmark() {
        return this.endBookmark;
    }

    public void setEndBookmark(PDOutlineItem aEndBookmark) {
        this.endBookmark = aEndBookmark;
    }

    public PDOutlineItem getStartBookmark() {
        return this.startBookmark;
    }

    public void setStartBookmark(PDOutlineItem aStartBookmark) {
        this.startBookmark = aStartBookmark;
    }

    public boolean getAddMoreFormatting() {
        return this.addMoreFormatting;
    }

    public void setAddMoreFormatting(boolean newAddMoreFormatting) {
        this.addMoreFormatting = newAddMoreFormatting;
    }

    public boolean getSortByPosition() {
        return this.sortByPosition;
    }

    public void setSortByPosition(boolean newSortByPosition) {
        this.sortByPosition = newSortByPosition;
    }

    public float getSpacingTolerance() {
        return this.spacingTolerance;
    }

    public void setSpacingTolerance(float spacingToleranceValue) {
        this.spacingTolerance = spacingToleranceValue;
    }

    public float getAverageCharTolerance() {
        return this.averageCharTolerance;
    }

    public void setAverageCharTolerance(float averageCharToleranceValue) {
        this.averageCharTolerance = averageCharToleranceValue;
    }

    public float getIndentThreshold() {
        return this.indentThreshold;
    }

    public void setIndentThreshold(float indentThresholdValue) {
        this.indentThreshold = indentThresholdValue;
    }

    public float getDropThreshold() {
        return this.dropThreshold;
    }

    public void setDropThreshold(float dropThresholdValue) {
        this.dropThreshold = dropThresholdValue;
    }

    public String getParagraphStart() {
        return this.paragraphStart;
    }

    public void setParagraphStart(String s) {
        this.paragraphStart = s;
    }

    public String getParagraphEnd() {
        return this.paragraphEnd;
    }

    public void setParagraphEnd(String s) {
        this.paragraphEnd = s;
    }

    public String getPageStart() {
        return this.pageStart;
    }

    public void setPageStart(String pageStartValue) {
        this.pageStart = pageStartValue;
    }

    public String getPageEnd() {
        return this.pageEnd;
    }

    public void setPageEnd(String pageEndValue) {
        this.pageEnd = pageEndValue;
    }

    public String getArticleStart() {
        return this.articleStart;
    }

    public void setArticleStart(String articleStartValue) {
        this.articleStart = articleStartValue;
    }

    public String getArticleEnd() {
        return this.articleEnd;
    }

    public void setArticleEnd(String articleEndValue) {
        this.articleEnd = articleEndValue;
    }

    private SCSPDFTextStripper.PositionWrapper handleLineSeparation(SCSPDFTextStripper.PositionWrapper current, SCSPDFTextStripper.PositionWrapper lastPosition, SCSPDFTextStripper.PositionWrapper lastLineStartPosition, float maxHeightForLine, SCSPDFTextStripper.PositionWrapper lastPositionNotSpace) throws IOException {
        current.setLineStart();
        this.isParagraphSeparation(current, lastPosition, lastLineStartPosition, maxHeightForLine, lastPositionNotSpace);
        if (current.isParagraphStart()) {
            if (lastPosition.isArticleStart()) {
                if (lastPosition.isLineStart()) {
                    this.writeLineSeparator();
                }

                this.writeParagraphStart();
            } else {
                this.writeLineSeparator();
                this.writeParagraphSeparator();
            }
        } else {
            this.writeLineSeparator();
        }

        return current;
    }

    private void isParagraphSeparation(SCSPDFTextStripper.PositionWrapper position, SCSPDFTextStripper.PositionWrapper lastPosition, SCSPDFTextStripper.PositionWrapper lastLineStartPosition, float maxHeightForLine, SCSPDFTextStripper.PositionWrapper lastPositionNotSpace) {


        boolean result = false;
        if (lastLineStartPosition == null) {
            result = true;
        } else {
            float yGap = Math.abs(position.getTextPosition().getYDirAdj() - lastPosition.getTextPosition().getYDirAdj());
            float newYVal = this.multiplyFloat(this.getDropThreshold(), maxHeightForLine);
            float xGap = position.getTextPosition().getXDirAdj() - lastLineStartPosition.getTextPosition().getXDirAdj();
            float newXVal = this.multiplyFloat(this.getIndentThreshold(), position.getTextPosition().getWidthOfSpace());
            float positionWidth = this.multiplyFloat(0.25F, position.getTextPosition().getWidth());
            if (yGap > newYVal) {
                result = true;
            } else if (xGap > newXVal) {
                if (!lastLineStartPosition.isParagraphStart()) {
                    result = true;
                } else {
                    position.setHangingIndent();
                }
            } else if (xGap < -position.getTextPosition().getWidthOfSpace()) {
                if (!lastLineStartPosition.isParagraphStart()) {
                    result = true;
                }
            } else if (Math.abs(xGap) < positionWidth) {
                if (lastLineStartPosition.isHangingIndent()) {
                    position.setHangingIndent();
                } else if (lastLineStartPosition.isParagraphStart()) {
                    Pattern liPattern = this.matchListItemPattern(lastLineStartPosition);
                    if (liPattern != null) {
                        Pattern currentPattern = this.matchListItemPattern(position);
                        if (liPattern == currentPattern) {
                            result = true;
                        }
                    }
                }
            }
        }

        StringBuilder builder = new StringBuilder();

        // todo new CharactersSpecificEndOfSentenceScanner('.', '?', '!', '"', '-', '…');
            if (lastPositionNotSpace != null && !lastPositionNotSpace.getTextPosition().getUnicode().equalsIgnoreCase(".")
                    && (position.getTextPosition().getFont().equals(lastPositionNotSpace.getTextPosition().getFont()))
                    && (position.getTextPosition().getFontSize() == (lastPositionNotSpace.getTextPosition().getFontSize()))) { //|| position.getFontSize() != prevPDFont.getStringWidth())
                    result = false;
            }

        if (result) {
            position.setParagraphStart();
        }

    }

    private float multiplyFloat(float value1, float value2) {
        return (float) Math.round(value1 * value2 * 1000.0F) / 1000.0F;
    }

    protected void writeParagraphSeparator() throws IOException {
        this.writeParagraphEnd();
        this.writeParagraphStart();
    }

    protected void writeParagraphStart() throws IOException {
        if (this.inParagraph) {
            this.writeParagraphEnd();
            this.inParagraph = false;
        }

        this.output.write(this.getParagraphStart());
        this.inParagraph = true;
    }

    protected void writeParagraphEnd() throws IOException {
        if (!this.inParagraph) {
            this.writeParagraphStart();
        }

        this.output.write(this.getParagraphEnd());
        this.inParagraph = false;
    }

    protected void writePageStart() throws IOException {
        this.output.write(this.getPageStart());
    }

    protected void writePageEnd() throws IOException {
        this.output.write(this.getPageEnd());
    }

    private Pattern matchListItemPattern(SCSPDFTextStripper.PositionWrapper pw) {
        TextPosition tp = pw.getTextPosition();
        String txt = tp.getUnicode();
        return matchPattern(txt, this.getListItemPatterns());
    }

    protected void setListItemPatterns(List<Pattern> patterns) {
        this.listOfPatterns = patterns;
    }

    protected List<Pattern> getListItemPatterns() {
        if (this.listOfPatterns == null) {
            this.listOfPatterns = new ArrayList();
            String[] var1 = LIST_ITEM_EXPRESSIONS;
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                String expression = var1[var3];
                Pattern p = Pattern.compile(expression);
                this.listOfPatterns.add(p);
            }
        }

        return this.listOfPatterns;
    }

    protected static Pattern matchPattern(String string, List<Pattern> patterns) {
        Iterator var2 = patterns.iterator();

        Pattern p;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            p = (Pattern) var2.next();
        } while (!p.matcher(string).matches());

        return p;
    }

    private void writeLine(List<SCSPDFTextStripper.WordWithTextPositions> line) throws IOException {
        int numberOfStrings = line.size();
        for (int i = 0; i < numberOfStrings; ++i) {
            SCSPDFTextStripper.WordWithTextPositions word = (SCSPDFTextStripper.WordWithTextPositions) line.get(i);
            //System.out.println("word.getText()="+ word.getText() + word.getTextPositions().get(0).getFont()+word.getTextPositions().get(0).getFontSize());
            this.writeString(word.getText(), word.getTextPositions());
            if (i < numberOfStrings - 1) {
                this.writeWordSeparator();
            }
        }

    }

    private List<SCSPDFTextStripper.WordWithTextPositions> normalize(List<SCSPDFTextStripper.LineItem> line) {
        List<SCSPDFTextStripper.WordWithTextPositions> normalized = new LinkedList();
        StringBuilder lineBuilder = new StringBuilder();
        List<TextPosition> wordPositions = new ArrayList();

        SCSPDFTextStripper.LineItem item;
        for (Iterator var5 = line.iterator(); var5.hasNext(); lineBuilder = this.normalizeAdd(normalized, lineBuilder, wordPositions, item)) {
            item = (SCSPDFTextStripper.LineItem) var5.next();
        }

        if (lineBuilder.length() > 0) {
            normalized.add(this.createWord(lineBuilder.toString(), wordPositions));
        }

        return normalized;
    }

    private String handleDirection(String word) {
        Bidi bidi = new Bidi(word, -2);
        if (!bidi.isMixed() && bidi.getBaseLevel() == 0) {
            return word;
        } else {
            int runCount = bidi.getRunCount();
            byte[] levels = new byte[runCount];
            Integer[] runs = new Integer[runCount];

            for (int i = 0; i < runCount; ++i) {
                levels[i] = (byte) bidi.getRunLevel(i);
                runs[i] = Integer.valueOf(i);
            }

            Bidi.reorderVisually(levels, 0, runs, 0, runCount);
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < runCount; ++i) {
                int index = runs[i].intValue();
                int start = bidi.getRunStart(index);
                int end = bidi.getRunLimit(index);
                int level = levels[index];
                if ((level & 1) != 0) {
                    while (true) {
                        --end;
                        if (end < start) {
                            break;
                        }

                        char character = word.charAt(end);
                        if (Character.isMirrored(word.codePointAt(end))) {
                            if (MIRRORING_CHAR_MAP.containsKey(Character.valueOf(character))) {
                                result.append(MIRRORING_CHAR_MAP.get(Character.valueOf(character)));
                            } else {
                                result.append(character);
                            }
                        } else {
                            result.append(character);
                        }
                    }
                } else {
                    result.append(word, start, end);
                }
            }

            return result.toString();
        }
    }

    private static void parseBidiFile(InputStream inputStream) throws IOException {
        LineNumberReader rd = new LineNumberReader(new InputStreamReader(inputStream));

        while (true) {
            String s;
            do {
                s = rd.readLine();
                if (s == null) {
                    return;
                }

                int comment = s.indexOf(35);
                if (comment != -1) {
                    s = s.substring(0, comment);
                }
            } while (s.length() < 2);

            StringTokenizer st = new StringTokenizer(s, ";");
            int nFields = st.countTokens();
            Character[] fields = new Character[nFields];

            for (int i = 0; i < nFields; ++i) {
                fields[i] = Character.valueOf((char) Integer.parseInt(st.nextToken().trim(), 16));
            }

            if (fields.length == 2) {
                MIRRORING_CHAR_MAP.put(fields[0], fields[1]);
            }
        }
    }

    private SCSPDFTextStripper.WordWithTextPositions createWord(String word, List<TextPosition> wordPositions) {
        return new SCSPDFTextStripper.WordWithTextPositions(this.normalizeWord(word), wordPositions);
    }

    private String normalizeWord(String word) {
        StringBuilder builder = null;
        int p = 0;
        int q = 0;

        for (int strLength = word.length(); q < strLength; ++q) {
            char c = word.charAt(q);
            if ('ﬀ' <= c && c <= '\ufdff' || 'ﹰ' <= c && c <= '\ufeff') {
                if (builder == null) {
                    builder = new StringBuilder(strLength * 2);
                }

                builder.append(word.substring(p, q));
                if (c != 'ﷲ' || q <= 0 || word.charAt(q - 1) != 1575 && word.charAt(q - 1) != 'ﺍ') {
                    builder.append(Normalizer.normalize(word.substring(q, q + 1), Form.NFKC).trim());
                } else {
                    builder.append("لله");
                }

                p = q + 1;
            }
        }

        if (builder == null) {
            return this.handleDirection(word);
        } else {
            builder.append(word.substring(p, q));
            return this.handleDirection(builder.toString());
        }
    }

    private StringBuilder normalizeAdd(List<SCSPDFTextStripper.WordWithTextPositions> normalized, StringBuilder lineBuilder, List<TextPosition> wordPositions, SCSPDFTextStripper.LineItem item) {
        if (item.isWordSeparator()) {
            normalized.add(this.createWord(lineBuilder.toString(), new ArrayList(wordPositions)));
            lineBuilder = new StringBuilder();
            wordPositions.clear();
        } else {
            TextPosition text = item.getTextPosition();
            lineBuilder.append(text.getUnicode());
            wordPositions.add(text);
        }

        return lineBuilder;
    }

    static {
        String path = null;
        String version = null;

        try {
            String className = SCSPDFTextStripper.class.getSimpleName().toLowerCase();
            String prop = className + ".indent";
            version = System.getProperty(prop);
            prop = className + ".drop";
            path = System.getProperty(prop);
        } catch (SecurityException var22) {
            ;
        }

        if (version != null && version.length() > 0) {
            try {
                defaultIndentThreshold = Float.parseFloat(version);
            } catch (NumberFormatException var21) {
                ;
            }
        }

        if (path != null && path.length() > 0) {
            try {
                defaultDropThreshold = Float.parseFloat(path);
            } catch (NumberFormatException var20) {
                ;
            }
        }

        boolean is16orLess = false;

        try {
            version = System.getProperty("java.specification.version");
            StringTokenizer st = new StringTokenizer(version, ".");
            int majorVersion = Integer.parseInt(st.nextToken());
            int minorVersion = 0;
            if (st.hasMoreTokens()) {
                minorVersion = Integer.parseInt(st.nextToken());
            }

            is16orLess = majorVersion == 1 && minorVersion <= 6;
        } catch (SecurityException var23) {
            ;
        } catch (NumberFormatException var24) {
            ;
        }

        useCustomQuickSort = !is16orLess;
        LIST_ITEM_EXPRESSIONS = new String[]{"\\.", "\\d+\\.", "\\[\\d+\\]", "\\d+\\)", "[A-Z]\\.", "[a-z]\\.", "[A-Z]\\)", "[a-z]\\)", "[IVXL]+\\.", "[ivxl]+\\."};
        MIRRORING_CHAR_MAP = new HashMap();
        path = "org/apache/pdfbox/resources/text/BidiMirroring.txt";
        InputStream input = SCSPDFTextStripper.class.getClassLoader().getResourceAsStream(path);

        try {
            if (input != null) {
                parseBidiFile(input);
            } else {
                LOG.warn("Could not find '" + path + "', mirroring char map will be empty: ");
            }
        } catch (IOException var18) {
            LOG.warn("Could not parse BidiMirroring.txt, mirroring char map will be empty: " + var18.getMessage());
        } finally {
            try {
                input.close();
            } catch (IOException var17) {
                LOG.error("Could not close BidiMirroring.txt ", var17);
            }

        }

    }

    private static final class PositionWrapper {
        private boolean isLineStart = false;
        private boolean isParagraphStart = false;
        private boolean isPageBreak = false;
        private boolean isHangingIndent = false;
        private boolean isArticleStart = false;
        private TextPosition position = null;

        PositionWrapper(TextPosition position) {
            this.position = position;
        }

        public TextPosition getTextPosition() {
            return this.position;
        }

        public boolean isLineStart() {
            return this.isLineStart;
        }

        public void setLineStart() {
            this.isLineStart = true;
        }

        public boolean isParagraphStart() {
            return this.isParagraphStart;
        }

        public void setParagraphStart() {
            this.isParagraphStart = true;
        }

        public boolean isArticleStart() {
            return this.isArticleStart;
        }

        public void setArticleStart() {
            this.isArticleStart = true;
        }

        public boolean isPageBreak() {
            return this.isPageBreak;
        }

        public void setPageBreak() {
            this.isPageBreak = true;
        }

        public boolean isHangingIndent() {
            return this.isHangingIndent;
        }

        public void setHangingIndent() {
            this.isHangingIndent = true;
        }
    }

    private static final class WordWithTextPositions {
        String text;
        List<TextPosition> textPositions;

        WordWithTextPositions(String word, List<TextPosition> positions) {
            this.text = word;
            this.textPositions = positions;
        }

        public String getText() {
            return this.text;
        }

        public List<TextPosition> getTextPositions() {
            return this.textPositions;
        }
    }

    private static final class LineItem {
        public static SCSPDFTextStripper.LineItem WORD_SEPARATOR = new SCSPDFTextStripper.LineItem();
        private final TextPosition textPosition;

        public static SCSPDFTextStripper.LineItem getWordSeparator() {
            return WORD_SEPARATOR;
        }

        private LineItem() {
            this.textPosition = null;
        }

        LineItem(TextPosition textPosition) {
            this.textPosition = textPosition;
        }

        public TextPosition getTextPosition() {
            return this.textPosition;
        }

        public boolean isWordSeparator() {
            return this.textPosition == null;
        }
    }
}
