#### 2.1.4
* When opening and importing files, background threads will now also create the FileTab content
* Added the ConcurrentSingleReader functional interface
* Fixed the visual bug that prevented the ImportDialog and ExportDialog from closing right away when the confirmation button is pressed
* Added string formatting for the footer informational text views to include a thousand separator

#### 2.1.1
* Changed ExportDialog class to match optics of the ImportDialog
* Changed file formatting. Improved structure and tabs removed

#### 2.1.0
* Added proportional values computation to the pie chart controller. Taking numerical values of a second column into consideration is optional and can be omitted. If so, then the original behaviour of computing the proportional occurrences of all keys in the first column is executed
* Added *setAllSettingsViews()* method in *SettingsListView* and *setAllSettingsViewsInList()* method in *ChartController* to allow for complete replacement of all SettingsViews without any animations
* Added *isEmpty()* method in *SettingsListView* to check if any views are set
* Fixed float and double additions in the *ColumnStats* class using the *BigDecimal* constructor that expects a String instead of a primitive double which increases precision
* Changed *computeSumFor()* method in *ColumnStats* to return the result as a double. The main controller now uses the returned value to quickly compute the average, without the need for an additional DataFrame API call
* Fixed bug that caused a context menu sort action of a column to not set the underlying tab as modified
* Added 'show release notes' link in snackbar after successful update. Its visible duration is increased
* Added 'RELEASE_NOTES' constant in *ResourceLocator* enum
* Refactored dialog states in the *Updater* class to make them more maintainable
* Refactored system browser calls in *Updater*
* Removed animation for placeholder label in the *FrameController* class to reduce idle CPU load
* Fixed header checkbox checked color in the *ImportDialog* class
* Added CSS ID for the separator TextField in the *ImportDialog* class. Improved styling is done in CSS
* Added spacing to the separator layout in the *ImportDialog* class

#### 2.0.0
* Changed namespace from *com.kilo52* to *com.raven*
* Added plot activities
* Added column statistics context menu
* Added column conversion context menu
* Added auto updating capabilities
* Added the feedback activity
* Added a dark DataFrameView theme and the option to change themes in the preferences activity
* Complete internal code revision

#### 1.0.0 
* Final version for open source release under the *com.kilo52* namespace

