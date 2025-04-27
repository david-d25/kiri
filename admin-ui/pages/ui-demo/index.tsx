import './ui-demo.module.scss'
import React, { useState } from 'react';
import Button from "../../components/Button/Button";
import InfoPanel from "../../components/InfoPanel/InfoPanel";
import Slider from "../../components/Slider/Slider";
import Checkbox from "../../components/Checkbox/Checkbox";
import Radio from "../../components/Radio/Radio";
import Dropdown from "../../components/Dropdown/Dropdown";
import Toggle from "../../components/Toggle/Toggle";
import Card from "../../components/Card/Card";
import Tooltip from "../../components/Tooltip/Tooltip";
import Divider from "../../components/Divider/Divider";
import Badge from "../../components/Badge/Badge";

import styles from "./ui-demo.module.scss"
import TextInput from "../../components/TextInput/TextInput";
import TextArea from "../../components/TextArea/TextArea";
import UserAvatar from "../../components/UserAvatar/UserAvatar";

export default function UiDemo() {
    // State for interactive components
    const [sliderValue, setSliderValue] = useState(50);
    const [isChecked, setIsChecked] = useState(false);
    const [radioValue, setRadioValue] = useState("option1");
    const [dropdownValue, setDropdownValue] = useState("option1");
    const [isToggled, setIsToggled] = useState(false);
    const [inputValue, setInputValue] = useState('');
    const [textAreaValue, setTextAreaValue] = useState('');
    // Options for dropdown and radio
    const options = [
        {label: "Option 1", value: "option1"},
        {label: "Option 2", value: "option2"},
        {label: "Option 3", value: "option3"},
    ];

    return (
        <div className={styles.uiDemo}>
            <h1>UI Demo</h1>

            <h2>Buttons</h2>
            <div className={styles.demoSection}>
                <Button>Button</Button>
                <Button disabled>Disabled</Button>
            </div>

            <h2>Info panels</h2>
            <div className={styles.demoSection}>
                <InfoPanel>Information panel</InfoPanel>
                <InfoPanel type="error">Error information panel</InfoPanel>
            </div>
            <div className={styles.demoSection}>
                <InfoPanel>
                    Information panel
                    <br/>
                    with a lot of
                    <br/>
                    lines
                </InfoPanel>
                <InfoPanel type="error">
                    Error information panel
                    <br/>
                    with a lot of
                    <br/>
                    lines
                </InfoPanel>
            </div>

            <h2>Slider</h2>
            <div className={styles.demoSection}>
                <Slider
                    value={sliderValue}
                    onChange={setSliderValue}
                />
                <Slider
                    value={50}
                    onChange={() => {
                    }}
                    disabled
                />
            </div>

            <h2>Checkbox</h2>
            <div className={styles.demoSection}>
                <Checkbox
                    label="Enable feature"
                    checked={isChecked}
                    onChange={setIsChecked}
                />
                <Checkbox
                    label="Disabled checkbox"
                    checked={true}
                    onChange={() => {
                    }}
                    disabled
                />
                <Checkbox
                    label="Disabled unchecked"
                    checked={false}
                    onChange={() => {
                    }}
                    disabled
                />
            </div>

            <h2>Radio buttons</h2>
            <div className={styles.demoSection}>
                <Radio
                    options={options}
                    selectedValue={radioValue}
                    name="demoRadio"
                    onChange={setRadioValue}
                />
                <div className={styles.spacer}></div>
                <Radio
                    options={options}
                    selectedValue="option2"
                    name="disabledRadio"
                    onChange={() => {
                    }}
                    disabled
                />
            </div>

            <h2>Dropdown</h2>
            <div className={styles.demoSection}>
                <Dropdown
                    options={options}
                    selectedValue={dropdownValue}
                    placeholder="Select an option"
                    onChange={setDropdownValue}
                />
                <div className={styles.spacer}></div>
                <Dropdown
                    options={options}
                    selectedValue="option3"
                    placeholder="Select an option"
                    onChange={() => {
                    }}
                    disabled
                />
            </div>

            <h2>Toggle switch</h2>
            <div className={styles.demoSection}>
                <Toggle
                    checked={isToggled}
                    label="Enable feature"
                    onChange={setIsToggled}
                />
                <Toggle
                    checked={true}
                    label="Disabled toggle"
                    onChange={() => {
                    }}
                    disabled
                />
            </div>

            <h2>Card</h2>
            <div className={styles.demoSection}>
                <Card title="Card Title">
                    <p>This is a card component with some content.</p>
                    <p>Cards can be used to group related information.</p>
                </Card>

                <Card
                    title="Card with Footer"
                    footer={<Button>Action</Button>}
                >
                    <p>This card has a footer with a button.</p>
                </Card>
            </div>

            <h2>Tooltip</h2>
            <div className={styles.demoSection}>
                <Tooltip content="This is a tooltip at the top" position="top">
                    <Button>Hover me (top)</Button>
                </Tooltip>

                <Tooltip content="This is a tooltip at the bottom" position="bottom">
                    <Button>Hover me (bottom)</Button>
                </Tooltip>

                <Tooltip content="This is a tooltip on the left" position="left">
                    <Button>Hover me (left)</Button>
                </Tooltip>

                <Tooltip content="This is a tooltip on the right" position="right">
                    <Button>Hover me (right)</Button>
                </Tooltip>
            </div>

            <h2>Divider</h2>
            <div className={styles.demoSection}>
                <div className={styles.dividerDemo}>
                    <p>Content above divider</p>
                    <Divider/>
                    <p>Content below divider</p>

                    <Divider text="With Text"/>

                    <p>More content</p>
                </div>

                <div className={styles.dividerVerticalDemo}>
                    <div>Column 1</div>
                    <Divider direction="vertical"/>
                    <div>Column 2</div>
                    <Divider direction="vertical" text="OR"/>
                    <div>Column 3</div>
                </div>
            </div>

            <h2>Badge</h2>
            <div className={styles.demoSection}>
                <Badge count={5}>
                    <Button>Notifications</Button>
                </Badge>

                <Badge count={99}>
                    <Button>Messages</Button>
                </Badge>

                <Badge count={100} max={99}>
                    <Button>99+ Items</Button>
                </Badge>

                <Badge dot>
                    <Button>New</Button>
                </Badge>
            </div>

            <h2>Badge Types</h2>
            <div className={styles.demoSection}>
                <Badge count={5} type="default">
                    <div className={styles.badgeBox}>Default</div>
                </Badge>

                <Badge count={5} type="primary">
                    <div className={styles.badgeBox}>Primary</div>
                </Badge>

                <Badge count={5} type="success">
                    <div className={styles.badgeBox}>Success</div>
                </Badge>

                <Badge count={5} type="warning">
                    <div className={styles.badgeBox}>Warning</div>
                </Badge>

                <Badge count={5} type="error">
                    <div className={styles.badgeBox}>Error</div>
                </Badge>
            </div>
            <h2>Text Input</h2>
            <div className={styles.demoSection}>
                <TextInput
                    value={inputValue}
                    onChange={setInputValue}
                    placeholder="Enter your name"
                    label="Name"
                />

                <TextInput
                    value=""
                    onChange={() => {}}
                    placeholder="Disabled input"
                    label="Disabled"
                    disabled
                />

                <TextInput
                    value="Invalid input"
                    onChange={() => {}}
                    label="With error"
                    error="This field is required"
                />

                <TextInput
                    value=""
                    onChange={() => {}}
                    placeholder="Password"
                    label="Password"
                    type="password"
                />
            </div>
            <h2>Text Area</h2>
            <div className={styles.demoSection}>
                <TextArea
                    value={textAreaValue}
                    onChange={setTextAreaValue}
                    placeholder="Write your message here..."
                    label="Message"
                />

                <TextArea
                    value="This is a disabled text area with some content."
                    onChange={() => {}}
                    label="Disabled"
                    disabled
                />

                <TextArea
                    value="Text with max length"
                    onChange={() => {}}
                    label="With max length"
                    maxLength={100}
                    rows={3}
                />
            </div>
            <h2>Avatar</h2>
            <div className={styles.demoSection}>
                <div className={styles.avatarGroup}>
                    <div>
                        <p>Small</p>
                        <UserAvatar
                            size="small"
                            name="John Doe"
                        />
                    </div>

                    <div>
                        <p>Medium</p>
                        <UserAvatar
                            size="medium"
                            name="Jane Smith"
                        />
                    </div>

                    <div>
                        <p>Large</p>
                        <UserAvatar
                            size="large"
                            name="Alex Johnson"
                        />
                    </div>

                    <div>
                        <p>With image</p>
                        <UserAvatar
                            size="large"
                            src="https://example.com/non-existent-image.jpg"
                            name="Robert Brown"
                        />
                    </div>
                </div>
            </div>
        </div>
    )
}