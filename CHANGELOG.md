#### 2.3.2
* Added licenses dialog in AboutController
* Changed *.desktop* entry and added *StartupWMClass* key
* Updated claymore lib patch version

#### 2.3.1
* Changed tooltip text for CharColumns
* Changed column construction in CreateDialogController and AddColumnDialogController
* Fixed bug in computation in column stats
* Removed unused and deprecated code
* Updated claymore lib version to 4.0.0 and API calls

#### 2.3.0
* Added drag event handlers for enabling file drag and drop opening behaviour
* Added selection entry for BinaryColumn type in CreateDialog and AddColumnDialog
* Added persistent configuration entries for basic plot functionalities
* Added associated drag callbacks in FXML markup and code to handle file drag and drop events
* Added Effects utility class for file drag and drop effects
* Added abstract application Cache class and SessionCache implementation
* Added SceneShowingProperty class to handle scene showing events
* Changed URLs in ResourceLocator class
* Changed ChartController implementations to adhere to persisted and cached configurations
* Changed SettingsView and its subclasses to check for session cache entries
* Changed SettingsView default colors
* Refactored plot activities argument handover
* Removed default plot color entries in CSS theme file
* Fixed NPE in history file setup in FrameController

#### 2.2.0
* Added support for BinaryColumns
* Fixed bug to prevent resource leak after an update when copying config template
* Fixed NPE in DataFrame sanitization
* Update dependencies to use claymore-3.0.1

#### 2.1.4
* Added string formatting for the footer informational text views to include a thousand separator
* Added the ConcurrentSingleReader functional interface
* Changed thread operation, when opening and importing files, background threads will now also create the FileTab content
* Fixed the visual bug that prevented the ImportDialog and ExportDialog from closing right away when the confirmation button is pressed

#### 2.1.1
* Changed ExportDialog class to match optics of the ImportDialog
* Changed file formatting. Improved structure and tabs removed

#### 2.1.0
* Added CSS ID for the separator TextField in the *ImportDialog* class. Improved styling is done in CSS
* Added spacing to the separator layout in the *ImportDialog* class
* Added proportional values computation to the pie chart controller. Taking numerical values of a second column into consideration is optional and can be omitted. If so, then the original behaviour of computing the proportional occurrences of all keys in the first column is executed
* Added *setAllSettingsViews()* method in *SettingsListView* and *setAllSettingsViewsInList()* method in *ChartController* to allow for complete replacement of all SettingsViews without any animations
* Added *isEmpty()* method in *SettingsListView* to check if any views are set
* Added 'show release notes' link in snackbar after successful update. Its visible duration is increased
* Added 'RELEASE_NOTES' constant in *ResourceLocator* enum
* Changed *computeSumFor()* method in *ColumnStats* to return the result as a double. The main controller now uses the returned value to quickly compute the average, without the need for an additional DataFrame API call
* Fixed float and double additions in the *ColumnStats* class using the *BigDecimal* constructor that expects a String instead of a primitive double which increases precision
* Fixed bug that caused a context menu sort action of a column to not set the underlying tab as modified
* Refactored dialog states in the *Updater* class to make them more maintainable
* Refactored system browser calls in *Updater*
* Removed animation for placeholder label in the *FrameController* class to reduce idle CPU load
* Fixed header checkbox checked color in the *ImportDialog* class

#### 2.0.0
* Added plot activities
* Added column statistics context menu
* Added column conversion context menu
* Added auto updating capabilities
* Added the feedback activity
* Added a dark DataFrameView theme and the option to change themes in the preferences activity
* Changed namespace from *com.kilo52* to *com.raven*
* Complete internal code revision

#### 1.0.0 
* Final version for open source release under the *com.kilo52* namespace

